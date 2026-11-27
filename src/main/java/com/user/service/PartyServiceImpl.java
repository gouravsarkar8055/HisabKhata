package com.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
<<<<<<< HEAD
import java.util.UUID;
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	
<<<<<<< HEAD
	@Autowired
	private NotificationPushService notificationPushService;
	
=======
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
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
<<<<<<< HEAD
	    	String idempotencyKey = UUID.randomUUID().toString();
	    	transactionServiceImpl.addTransaction(idempotencyKey, t);
	    }
	    notificationPushService.sendToUser(
        		transactionServiceImpl.getLoggedInUserId(),
                1l,
                "HisabKhata",
                "New Party added successfully",
                "USER",
                null
            );
=======
	    	
	    	transactionServiceImpl.addTransaction(t);
	    }
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
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

	    if(existingParty.getBalance() != prt.getBalance()) {
	    	if(prt.getBalance() != null && prt.getBalance() != 0L) {
	    		Long transactionIdByEntityIdAndTransaction = 
	    				transactionRepo.getTransactionIdByEntityIdAndTransaction(prt.getId(), "opening-balance");
	    		if(transactionIdByEntityIdAndTransaction != null) {
	    			Transaction transaction = transactionRepo.findByTransactionId(transactionIdByEntityIdAndTransaction);
	    			transaction.setAmount(prt.getBalance());
	    			transaction.setDateOfTransaction(prt.getDateOfLastTransaction());
	    			transaction.setEntityKey(prt.getPartyKey());
	    			transactionRepo.save(transaction);
	    		}
	    		else {
	    			Transaction t = new Transaction();
			    	t.setAmount(prt.getBalance());
			    	t.setCreatedBy(userId);
			    	t.setDateOfTransaction(prt.getDateOfLastTransaction());
			    	t.setTransaction("opening-balance");
			    	t.setEntityKey(prt.getPartyKey());
			    	t.setEntityId(prt.getId());
<<<<<<< HEAD
			    	String idempotencyKey = UUID.randomUUID().toString();
			    	transactionServiceImpl.addTransaction(idempotencyKey, t);
=======
			    	
			    	transactionServiceImpl.addTransaction(t);
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
	    		}
		    }
	    }
	    
	    // 4️⃣ Update fields
	    existingParty.setName(prt.getName());
	    existingParty.setEmail(prt.getEmail());
	    existingParty.setAddress(prt.getAddress());
	    existingParty.setBalance(prt.getBalance() != null ? prt.getBalance() : 0L);
	    existingParty.setPhone(prt.getPhone());
	    existingParty.setPartyKey(prt.getPartyKey()); // if needed
	    existingParty.setDateOfLastTransaction(prt.getDateOfLastTransaction());

	    PartyDetails updated = partyDetailsRepo.save(existingParty);

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
	
	public ResponseEntity<ApiResponse<Object>> getAllParties(
	        int page,
	        int size,
	        String sortBy,
	        String direction,
	        Character key) {

	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();

	    Sort sort = direction.equalsIgnoreCase("desc")
	            ? Sort.by(sortBy).descending()
	            : Sort.by(sortBy).ascending();

	    Pageable pageable = PageRequest.of(page, size, sort);

	    Page<PartyDetails> partyPage=null;
	    if(!key.equals('A')) {
	    partyPage =
	            partyDetailsRepo.findParties(
	                    userId,
	                    key,
	                    pageable
	            );
	    }
	    else {
	    partyPage =
	            partyDetailsRepo.findAllByCreatedBy(userId, pageable);
	    }
	    if (partyPage.isEmpty()) {
	        return ResponseEntity.ok(
	                ApiResponse.builder()
	                        .success(true)
	                        .message("No Details Found")
	                        .data(null)
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }

	    // 🔥 Modify Balance Logic Here
	    List<Map<String, Object>> modifiedList = partyPage.getContent()
	            .stream()
	            .map(party -> {

	                Map<String, Object> partyMap = new HashMap<>();
	                partyMap.put("id", party.getId());
	                partyMap.put("name", party.getName());
	                partyMap.put("phone", party.getPhone());
	                partyMap.put("email", party.getEmail());
	                partyMap.put("address", party.getAddress());
	                partyMap.put("createdBy", party.getCreatedBy());
	                partyMap.put("partyKey", party.getPartyKey());
	                partyMap.put("dateOfLastTransaction", party.getDateOfLastTransaction());
	                partyMap.put("dateOfJoin", party.getDateOfJoin());
	                Long balance = party.getBalance();

	                if (balance != null) {
	                    if (balance < 0) {
	                        partyMap.put("balance", Math.abs(balance)); // remove - sign
	                        partyMap.put("balanceType", "You'll Pay");
	                        partyMap.put("balanceColor", "#960E03");
	                    } else if (balance > 0) {
	                        partyMap.put("balance", balance);
	                        partyMap.put("balanceType", "You'll Receive");
	                        partyMap.put("balanceColor", "#038408");
	                    } else {
	                        partyMap.put("balance", 0);
	                        partyMap.put("balanceType", "Settled");
	                    }
	                }

	                return partyMap;
	            })
	            .toList();

	    Map<String, Object> responseData = new HashMap<>();
	    responseData.put("data", modifiedList);
	    responseData.put("currentPage", partyPage.getNumber());
	    responseData.put("totalItems", partyPage.getTotalElements());
	    responseData.put("totalPages", partyPage.getTotalPages());
	    responseData.put("pageSize", partyPage.getSize());

	    return ResponseEntity.ok(
	            ApiResponse.builder()
	                    .success(true)
	                    .message("Party list fetched successfully")
	                    .data(responseData)
	                    .timestamp(LocalDateTime.now())
	                    .build()
	    );
	}
	
	public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllPartiesName() {
	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    List<Map<String,Object>> allPartiesIdName = partyDetailsRepo.getAllPartiesIdName(userId);
	    
	    if (allPartiesIdName == null) {
	        return ResponseEntity.ok(
	                ApiResponse.<List<Map<String, Object>>>builder()
	                        .success(true)
	                        .message("No Details Found")
	                        .data(null)
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }
	    
	    return ResponseEntity.ok(
	            ApiResponse.<List<Map<String, Object>>>builder()
	                    .success(true)
	                    .message("Party names fetched successfully")
	                    .data(allPartiesIdName)
	                    .timestamp(LocalDateTime.now())
	                    .build()
	    );
	}
	
	public ResponseEntity<ApiResponse<PartyDetails>> getPartyById(Long partyId) {
	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    PartyDetails party = partyDetailsRepo.getPartyById(userId, partyId);
	    
	    if (party == null) {
	        return ResponseEntity.ok(
	                ApiResponse.<PartyDetails>builder()
	                        .success(true)
	                        .message("No Details Found")
	                        .data(null)
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }
	    
	    return ResponseEntity.ok(
	            ApiResponse.<PartyDetails>builder()
	                    .success(true)
	                    .message("Party fetched successfully")
	                    .data(party)
	                    .timestamp(LocalDateTime.now())
	                    .build()
	    );
	}
	
	public ResponseEntity<ApiResponse<PartyDetails>> getPartyByName(String partyName) {
	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    PartyDetails party = partyDetailsRepo.getPartyByName(userId, partyName);
	    
	    if (party == null) {
	        return ResponseEntity.ok(
	                ApiResponse.<PartyDetails>builder()
	                        .success(true)
	                        .message("No Details Found")
	                        .data(null)
	                        .timestamp(LocalDateTime.now())
	                        .build()
	        );
	    }
	    
	    return ResponseEntity.ok(
	            ApiResponse.<PartyDetails>builder()
	                    .success(true)
	                    .message("Party fetched successfully")
	                    .data(party)
	                    .timestamp(LocalDateTime.now())
	                    .build()
	    );
	}
}
