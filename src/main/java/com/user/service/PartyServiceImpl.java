package com.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.user.api_response.ApiResponse;
import com.user.entity.PartyDetails;
import com.user.entity.Transaction;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.repository.PartyDetailsRepo;
import com.user.repository.TransactionRepo;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class PartyServiceImpl {
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Autowired
	private Validator validator;
	
	@Autowired
	private PartyDetailsRepo partyDetailsRepo;
	
	@Autowired
	private TransactionRepo transactionRepo;
	
	@Autowired
	private TransactionServiceImpl transactionServiceImpl;
	
	public ApiResponse<PartyDetails> addParty(PartyDetails prt) {

	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();

	    // 0️⃣ Set createdBy FIRST so duplicate check works
	    prt.setCreatedBy(userId);

	    if (prt.getBalance() == null) {
	        prt.setBalance(0L);
	    }
	    
	    Map<String, String> errors = new HashMap<>();

	    // 1️⃣ Duplicate name check (NOW it works)
	    if (partyDetailsRepo.existsByNameAndCreatedBy(prt.getName(), userId)) {
	        errors.put("name", "Party name already exists");
	    }

	    // 2️⃣ Bean Validation errors
	    Set<ConstraintViolation<PartyDetails>> violations = validator.validate(prt);
	    for (ConstraintViolation<PartyDetails> v : violations) {
	        errors.put(v.getPropertyPath().toString(), v.getMessage());
	    }

	    // 3️⃣ Return errors if any
	    if (!errors.isEmpty()) {
	        return ApiResponse.<PartyDetails>builder()
	                .success(false)
	                .message("Validation failed")
	                .errors(errors)
	                .timestamp(LocalDateTime.now())
	                .build();
	    }

	    // 4️⃣ Save
	    PartyDetails saved = partyDetailsRepo.save(prt);

	    if(prt.getBalance() != null && prt.getBalance() != 0L) {
	    	Transaction t = new Transaction();
	    	t.setAmount(prt.getBalance());
	    	t.setCreatedBy(userId);
	    	t.setDateOfTransaction(prt.getDateOfLastTransaction());
	    	t.setTransaction("opening-balance");
	    	t.setEntityKey(prt.getPartyKey());
	    	t.setEntityId(saved.getId());
	    	
	    	transactionServiceImpl.addTransaction(t);
	    }
	    return ApiResponse.<PartyDetails>builder()
	            .success(true)
	            .message("Party added successfully")
	            .data(saved)
	            .timestamp(LocalDateTime.now())
	            .build();
	}
	
	public ApiResponse<PartyDetails> updateParty(PartyDetails prt) {

	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();

	    // 1️⃣ Validate fields using javax validator
	    Set<ConstraintViolation<PartyDetails>> violations = validator.validate(prt);
	    Map<String, String> errors = new HashMap<>();

	    if (!violations.isEmpty()) {
	        for (ConstraintViolation<PartyDetails> v : violations) {
	            errors.put(v.getPropertyPath().toString(), v.getMessage());
	        }

	        return ApiResponse.<PartyDetails>builder()
	                .success(false)
	                .message("Validation failed")
	                .errors(errors)
	                .timestamp(LocalDateTime.now())
	                .build();
	    }

	    // 2️⃣ Check if party exists
	    Optional<PartyDetails> existingOpt = partyDetailsRepo.findById(prt.getId());
	    if (existingOpt.isEmpty()) {
	        return ApiResponse.<PartyDetails>builder()
	                .success(false)
	                .message("Party not found")
	                .timestamp(LocalDateTime.now())
	                .build();
	    }

	    PartyDetails existingParty = existingOpt.get();

	    // 3️⃣ Duplicate name check (ignore same record)
	    boolean nameTaken =
	            partyDetailsRepo.existsByNameAndCreatedBy(prt.getName(), userId)
	                    && !existingParty.getName().equals(prt.getName());

	    if (nameTaken) {
	        errors.put("name", "Party name already exists");

	        return ApiResponse.<PartyDetails>builder()
	                .success(false)
	                .message("Validation failed")
	                .errors(errors)
	                .timestamp(LocalDateTime.now())
	                .build();
	    }

	    if (prt.getBalance() == null) {
	        prt.setBalance(0L);
	    }
	    // 4️⃣ Update fields
	    existingParty.setName(prt.getName());
	    existingParty.setEmail(prt.getEmail());
	    existingParty.setAddress(prt.getAddress());
	    existingParty.setBalance(prt.getBalance());
	    existingParty.setPhone(prt.getPhone());
	    existingParty.setPartyKey(prt.getPartyKey()); // if needed
	    existingParty.setDateOfLastTransaction(prt.getDateOfLastTransaction());

	    PartyDetails updated = partyDetailsRepo.save(existingParty);
	    
	    if(existingParty.getBalance() == prt.getBalance()) {
	    	
	    }

	    return ApiResponse.<PartyDetails>builder()
	            .success(true)
	            .message("Party updated successfully")
	            .data(updated)
	            .timestamp(LocalDateTime.now())
	            .build();
	}

	@Transactional
	public ResponseEntity<ApiResponse<?>> deleteParty(Long partyId) {

	    Optional<PartyDetails> foundParty = partyDetailsRepo.findById(partyId);

	    // 1️⃣ Party not found
	    if (foundParty.isEmpty()) {
	        return ResponseEntity.badRequest().body(
	                ApiResponse.builder()
	                        .success(false)
	                        .message("Party not found")
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }

	    PartyDetails party = foundParty.get();

	    // 2️⃣ Check if this party has any transactions
	    int transactionCount = transactionRepo.countByEntityId(partyId);

	    if (transactionCount > 0) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(
	                ApiResponse.builder()
	                        .success(false)
	                        .message("Cannot delete '" + party.getName() + "'. It is linked with existing transactions.")
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }

	    try {
	        // 3️⃣ Delete Party
	        partyDetailsRepo.deleteById(partyId);

	        return ResponseEntity.ok(
	                ApiResponse.builder()
	                        .success(true)
	                        .message("Party '" + party.getName() + "' has been deleted successfully.")
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                ApiResponse.builder()
	                        .success(false)
	                        .message("Something went wrong while deleting party.")
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }
	}
	
	public ResponseEntity<?> getAllParties(){
    	Long userId = jwtAuthenticationFilter.getLoggedInUserId(); 
		List<PartyDetails> parties = partyDetailsRepo.findAllByCreatedBy(userId);
		
		if (parties != null && !parties.isEmpty()) {
	        List<Map<String, Object>> partyDetails = new ArrayList<>();
	        for (PartyDetails party : parties) {
	            Map<String, Object> element = new HashMap<>();
	            element.put("id", party.getId());
	            element.put("name", party.getName());
	            element.put("email", party.getEmail());
	            element.put("dateOfLastTransaction", party.getDateOfLastTransaction());
	            element.put("balance", party.getBalance());
	            element.put("address", party.getAddress());
	            element.put("phone", party.getPhone());
	            element.put("dateOfJoin", party.getDateOfJoin());
	            element.put("partyKey", party.getPartyKey());
	            partyDetails.add(element);
	        }
	        
	        return new ResponseEntity<>(partyDetails, HttpStatus.OK);
	    }
		return new ResponseEntity<>("No Details", HttpStatus.OK);
    }
	
	public List<Map<String, Object>> getAllPartiesName() {
	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    return partyDetailsRepo.getAllPartiesIdName(userId);
	}
}
