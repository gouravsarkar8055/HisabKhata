package com.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import com.user.api_response.ApiResponse;
import com.user.dto.DashboardSummaryDTO;
import com.user.entity.BusinessEntity;
import com.user.entity.Employee;
import com.user.entity.IdempotencyEntity;
import com.user.entity.Employee;
import com.user.entity.PartyDetails;
import com.user.entity.Transaction;
import com.user.entity.TransactionPayment;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.repository.BusinessRepository;
import com.user.repository.EmployeeRepo;
import com.user.repository.IdempotencyRepository;
import com.user.repository.EmployeeRepo;
import com.user.repository.PartyDetailsRepo;
import com.user.repository.TransactionRepo;

import org.springframework.data.domain.Sort;

import org.springframework.transaction.annotation.Transactional;


@Service
public class TransactionServiceImpl {
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private PartyDetailsRepo partyDetailsRepo;
	@Autowired
	private EmployeeRepo employeeRepo;
	@Autowired
	private TransactionRepo transactionRepo;
	@Autowired
	private IdempotencyRepository idempotencyRepository;
	@Autowired
    private BusinessRepository businessRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
	
	public Long getLoggedInUserId() {
		return jwtAuthenticationFilter.getLoggedInUserId();
	}
	
	@Transactional
	public ApiResponse<Transaction> addTransaction(String idempotencyKey, Transaction transaction) {

	    logger.info("Inside addTransaction method");

	    Long userId = transaction.getCreatedBy();
	    BusinessEntity findByUserId = businessRepository.findByUserId(userId);
	    
	    if(findByUserId == null) {
	    	logger.info("No Business Found for this user.");

	        return ApiResponse.<Transaction>builder()
	                .success(true)
	                .message("Please add Business details ..")
	                .timestamp(LocalDateTime.now())
	                .httpStatus(HttpStatus.OK)
	                .build();
	    }
	    
	    Optional<IdempotencyEntity> existing =
	            idempotencyRepository.findByIdempotencyKey(idempotencyKey);

	    if (existing.isPresent()) {

	        Transaction oldTransaction = transactionRepo
	                .findById(existing.get().getTransactionId())
	                .orElse(null);

	        logger.info("Duplicate request. Returning existing transaction.");

	        return ApiResponse.<Transaction>builder()
	                .success(true)
	                .message("Transaction already processed")
	                .data(oldTransaction)
	                .timestamp(LocalDateTime.now())
	                .httpStatus(HttpStatus.OK)
	                .build();
	    }

	    if (userId == null) {
	        userId = jwtAuthenticationFilter.getLoggedInUserId();
	        transaction.setCreatedBy(userId);
	    }

	    Long payment = transaction.getPayment() == null ? 0L : transaction.getPayment();
	    Long discount = transaction.getDiscount() == null ? 0L : transaction.getDiscount();
	    Long amount = transaction.getAmount() == null ? 0L : transaction.getAmount();

	    Long balance = 0L;

	    if (transaction.getEntityId() != null) {

	        Long previousBalance = partyDetailsRepo.findBalanceById(transaction.getEntityId());
	        if (previousBalance == null) previousBalance = 0L;

	        Long totalBalance = 0L;

	        if (transaction.getTransaction().equalsIgnoreCase("payment-in")) {

	            balance = amount;
	            transaction.setBalance(balance);

	            totalBalance = previousBalance - amount;

	        } else if (transaction.getTransaction().equalsIgnoreCase("purchase")) {

	            balance = amount - (payment + discount);
	            transaction.setBalance(balance);

	            if (balance < 0) {
	                throw new RuntimeException("Paying amount is more than purchase amount.");
	            }

	            totalBalance = previousBalance - transaction.getBalance();

	        } else if (transaction.getTransaction().equalsIgnoreCase("payment-out")) {

	            balance = amount;
	            transaction.setBalance(balance);

	            totalBalance = previousBalance + amount;

	        } else if (transaction.getTransaction().equalsIgnoreCase("sale")) {

	            balance = amount - (payment + discount);
	            transaction.setBalance(balance);

	            if (balance < 0) {
	                throw new RuntimeException("Receiving amount is more than sale amount.");
	            }

	            totalBalance = previousBalance + transaction.getBalance();
	        }

	        LocalDate lastDate = partyDetailsRepo.findDateOfLastTransactionById(transaction.getEntityId());
	        LocalDate txDate = transaction.getDateOfTransaction();

	        if (lastDate != null && !txDate.isAfter(lastDate)) {
	            partyDetailsRepo.updateBalance(transaction.getEntityId(), totalBalance);
	        } else {
	            partyDetailsRepo.updateBalanceAndLastTransaction(
	                    transaction.getEntityId(),
	                    totalBalance,
	                    transaction.getDateOfTransaction()
	            );
	        }
	    }

	    if (transaction.getPayments() != null) {
	        for (TransactionPayment pay : transaction.getPayments()) {
	            pay.setTransaction(transaction);
	        }
	    }

	    Transaction savedTransaction = transactionRepo.save(transaction);
	    
	    IdempotencyEntity idempotency = new IdempotencyEntity();

	    idempotency.setIdempotencyKey(idempotencyKey);
	    idempotency.setUserId(savedTransaction.getCreatedBy());
	    idempotency.setTransactionId(savedTransaction.getId());
	    idempotency.setCreatedAt(LocalDateTime.now());

	    idempotencyRepository.save(idempotency);

	    logger.info("Saved new transaction ID: {}, Balance: {}",
	            savedTransaction.getId(), savedTransaction.getBalance());

	    return ApiResponse.<Transaction>builder()
	            .success(true)
	            .message("Transaction Successful")
	            .timestamp(LocalDateTime.now())
	            .httpStatus(HttpStatus.OK)
	            .build();
	}		
	
	@Transactional
	@CachePut(value = "transactions", key = "#p0.id")
	public ApiResponse<Transaction> editTransaction(Transaction transaction) {

	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();

	    Transaction existing = transactionRepo.findById(transaction.getId())
	            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

	    Long oldPartyBalance = partyDetailsRepo.findBalanceById(existing.getEntityId());
	    if (oldPartyBalance == null) oldPartyBalance = 0L;

	    Long reversedBalance = 0L;

	    if (existing.getTransaction().equalsIgnoreCase("payment-in"))
	        reversedBalance = oldPartyBalance + existing.getAmount();

	    else if (existing.getTransaction().equalsIgnoreCase("purchase"))
	        reversedBalance = oldPartyBalance + existing.getBalance();

	    else if (existing.getTransaction().equalsIgnoreCase("payment-out"))
	        reversedBalance = oldPartyBalance - existing.getAmount();

	    else if (existing.getTransaction().equalsIgnoreCase("sale"))
	        reversedBalance = oldPartyBalance - existing.getBalance();

	    Long payment = transaction.getPayment() == null ? 0L : transaction.getPayment();
	    Long discount = transaction.getDiscount() == null ? 0L : transaction.getDiscount();
	    Long amount = transaction.getAmount() == null ? 0L : transaction.getAmount();

	    Long newBalance = 0L;

	    if (transaction.getEntityId() != null) {

	        Long updatedFinalBalance = 0L;

	        if (transaction.getTransaction().equalsIgnoreCase("payment-in")) {

	            newBalance = amount;
	            updatedFinalBalance = reversedBalance - newBalance;

	        } else if (transaction.getTransaction().equalsIgnoreCase("purchase")) {

	            newBalance = amount - (payment + discount);

	            if (newBalance < 0) {
	                throw new RuntimeException("Paying amount is more than purchase amount.");
	            }

	            updatedFinalBalance = reversedBalance - newBalance;

	        } else if (transaction.getTransaction().equalsIgnoreCase("payment-out")) {

	            newBalance = amount;
	            updatedFinalBalance = reversedBalance + newBalance;

	        } else if (transaction.getTransaction().equalsIgnoreCase("sale")) {

	            newBalance = amount - (payment + discount);

	            if (newBalance < 0) {
	                throw new RuntimeException("Receiving amount is more than sale amount.");
	            }

	            updatedFinalBalance = reversedBalance + newBalance;
	        }

	        LocalDate lastDate = partyDetailsRepo.findDateOfLastTransactionById(transaction.getEntityId());
	        LocalDate txDate = transaction.getDateOfTransaction();

	        if (lastDate != null && !txDate.isAfter(lastDate)) {
	            partyDetailsRepo.updateBalance(transaction.getEntityId(), updatedFinalBalance);
	        } else {
	            partyDetailsRepo.updateBalanceAndLastTransaction(
	                    transaction.getEntityId(),
	                    updatedFinalBalance,
	                    txDate
	            );
	        }
	    }

	    // ✅ Clear old payments safely
	    existing.getPayments().clear();

	    // ✅ Add new payments
	    if (transaction.getPayments() != null) {
	        for (TransactionPayment incoming : transaction.getPayments()) {

	            TransactionPayment p = new TransactionPayment();
	            p.setAmount(incoming.getAmount());
	            p.setMode(incoming.getMode());
	            p.setBankName(incoming.getBankName());
	            p.setTransaction(existing);

	            existing.getPayments().add(p);
	        }
	    }

	    // ✅ Update fields
	    existing.setBalance(newBalance);
	    existing.setAmount(transaction.getAmount());
	    existing.setDescription(transaction.getDescription());
	    existing.setDiscount(transaction.getDiscount());
	    existing.setEntityId(transaction.getEntityId());
	    existing.setEntityKey(transaction.getEntityKey());
	    existing.setMode(transaction.getMode());
	    existing.setTransaction(transaction.getTransaction());
	    existing.setDateOfTransaction(transaction.getDateOfTransaction());
	    existing.setPayment(transaction.getPayment());
	    existing.setCreatedBy(userId);

	    if (transaction.getProductsJson() != null && !transaction.getProductsJson().isEmpty()) {
	        existing.setProducts(transaction.getProducts());
	    }

	    Transaction savedTransaction = transactionRepo.save(existing);

	    return ApiResponse.<Transaction>builder()
	            .success(true)
	            .message("Transaction Edited Successful")
	            .timestamp(LocalDateTime.now())
	            .build();
	}
	
	@Transactional
	@CacheEvict(value = "transactions", key = "#p0", beforeInvocation = true)
	public ApiResponse<?> deleteTransaction(Long transactionId) {

	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();

	    Transaction transaction = transactionRepo.findById(transactionId)
	            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

	    // ❌ Not created by same user => deny
	    if (!transaction.getCreatedBy().equals(userId)) {
	        throw new IllegalArgumentException("User not authorized to delete this transaction");
	    }

	    // 🔹 Reverse old balance
	    Long oldPartyBalance = partyDetailsRepo.findBalanceById(transaction.getEntityId());
	    if (oldPartyBalance == null) oldPartyBalance = 0L;

	    Long reversedBalance = 0L;

	    if (transaction.getTransaction().equalsIgnoreCase("payment-in")) {
	        reversedBalance = oldPartyBalance + transaction.getAmount();

	    } else if (transaction.getTransaction().equalsIgnoreCase("purchase")) {
	        reversedBalance = oldPartyBalance + transaction.getBalance();

	    } else if (transaction.getTransaction().equalsIgnoreCase("payment-out")) {
	        reversedBalance = oldPartyBalance - transaction.getAmount();

	    } else if (transaction.getTransaction().equalsIgnoreCase("sale")) {
	        reversedBalance = oldPartyBalance - transaction.getBalance();
	    }

	    // ✅ Clear payments (cascade handles delete)
	    transaction.getPayments().clear();

	    // ✅ Delete transaction
	    transactionRepo.delete(transaction);

	    // 🔹 Get last transaction date AFTER delete
	    LocalDate lastDate = transactionRepo.getLastTransactionDate(
	            transaction.getEntityKey(),
	            transaction.getEntityId()
	    );

	    // ✅ Update party balance
	    partyDetailsRepo.updateBalanceAndLastTransaction(
	            transaction.getEntityId(),
	            reversedBalance,
	            lastDate
	    );

	    return ApiResponse.builder()
	            .success(true)
	            .message("Transaction deleted successfully")
	            .timestamp(LocalDateTime.now())
	            .build();
	}
	
	//@Cacheable(value = "transactions")
	public ResponseEntity<?> getAllTransactions(int page, int size) {

	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();

	    Pageable pageable = PageRequest.of(page, size, Sort.by("dateOfTransaction").descending().and(Sort.by("id").descending()));

	    Page<Transaction> transactionPage =
	            transactionRepo.findAllByCreatedBy(userId, pageable);

	    if (!transactionPage.hasContent()) {
	        return new ResponseEntity<>("No Details", HttpStatus.OK);
	    }

	    List<Transaction> txList = transactionPage.getContent();

	    // STEP 1: Collect all IDs by type
	    Set<Long> partyIds = new HashSet<>();
	    Set<Long> employeeIds = new HashSet<>();

	    for (Transaction t : txList) {
	        if (t.getEntityKey() == 'C' || t.getEntityKey() == 'D') {
	            partyIds.add(t.getEntityId());
	        } else if (t.getEntityKey() == 'E') {
	            employeeIds.add(t.getEntityId());
	        }
	    }

	    // STEP 2: Fetch in BULK (one query each)
	    Map<Long, PartyDetails> partyMap = new HashMap<>();
	    if (!partyIds.isEmpty()) {
	        List<PartyDetails> parties = partyDetailsRepo.findAllById(partyIds);
	        for (PartyDetails p : parties) partyMap.put(p.getId(), p);
	    }

	    Map<Long, Employee> employeeMap = new HashMap<>();
	    if (!employeeIds.isEmpty()) {
	        List<Employee> employees = employeeRepo.findAllById(employeeIds);
	        for (Employee e : employees) employeeMap.put(e.getId(), e);
	    }

	    // STEP 3: Build final response
	    List<Map<String, Object>> responseList = new ArrayList<>();

	    for (Transaction t : txList) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("id", t.getId());
	        map.put("amount", t.getAmount());
	        map.put("balance", t.getBalance());
	        map.put("dateOfTransaction", t.getDateOfTransaction());
	        map.put("discount", t.getDiscount());
	        map.put("entityId", t.getEntityId());
	        map.put("entityKey", t.getEntityKey());
	        map.put("mode", t.getMode());
	        map.put("transaction", t.getTransaction().toUpperCase());
	        map.put("payment", t.getPayment());
	        if(t.getPayments() != null)
	        	map.put("payments", t.getPayments());
	        	
	        if (t.getProductsJson() != null)
	            map.put("products", t.getProducts());

	        // Instead of findById — O(1) lookup now
	        if (t.getEntityKey() == 'C' || t.getEntityKey() == 'D') {
	            map.put("personData", partyMap.get(t.getEntityId()));
	        } else if (t.getEntityKey() == 'E') {
	            map.put("personData", employeeMap.get(t.getEntityId()));
	        }

	        responseList.add(map);
	    }

	    // STEP 4: Return paginated response
	    Map<String, Object> finalResponse = new HashMap<>();
	    finalResponse.put("content", responseList);
	    finalResponse.put("currentPage", transactionPage.getNumber());
	    finalResponse.put("totalPages", transactionPage.getTotalPages());
	    finalResponse.put("totalElements", transactionPage.getTotalElements());
	    finalResponse.put("size", transactionPage.getSize());
	    finalResponse.put("isLast", transactionPage.isLast());

	    return new ResponseEntity<>(finalResponse, HttpStatus.OK);
	}
	
	public ResponseEntity<?> getTransactionDetailById(Long transactionId){
		Long userId = jwtAuthenticationFilter.getLoggedInUserId(); 
		Optional<Transaction> transactionById = transactionRepo.findById(transactionId);
		if(transactionById != null) {
			Transaction transaction = transactionRepo.findByTransactionId(transactionId);
		
		if (transaction != null) {
	            Map<String, Object> element = new HashMap<>();
	            element.put("id", transaction.getId());
	            element.put("amount", transaction.getAmount());
	            element.put("dateOfTransaction", transaction.getDateOfTransaction());
	            element.put("discount", transaction.getDiscount());
	            element.put("entityId", transaction.getEntityId());
	            element.put("entityKey", transaction.getEntityKey());
	            element.put("mode", transaction.getMode());
	            element.put("transaction", transaction.getTransaction());
	            if(transaction.getProductsJson() != null)
	            	element.put("products", transaction.getProducts());
	        
	        return new ResponseEntity<>(element, HttpStatus.OK);
	    }
	}
		return new ResponseEntity<>("No Detail", HttpStatus.OK);
	}
	
	public ResponseEntity<?> getTransactionByEntityKeyAndId(Character entityKey, Long entityId){
		Long userId = jwtAuthenticationFilter.getLoggedInUserId(); 
		if(userId != null) {
			List<Transaction> transactions = transactionRepo.findByEntityKeyAndId(entityId, entityKey, userId);
			if (transactions != null && !transactions.isEmpty()) {
		        List<Map<String, Object>> transactionDetails = new ArrayList<>();
		        for (Transaction transaction : transactions) {
		            Map<String, Object> element = new HashMap<>();
		            element.put("id", transaction.getId());
		            element.put("amount", transaction.getAmount());
		            element.put("previousBalance", transaction.getPreviousBalance());
		            element.put("balance", transaction.getBalance());
		            element.put("dateOfTransaction", transaction.getDateOfTransaction());
		            element.put("discount", transaction.getDiscount());
		            element.put("entityId", transaction.getEntityId());
		            element.put("entityKey", transaction.getEntityKey());
		            element.put("mode", transaction.getMode());
		            element.put("transaction", transaction.getTransaction());
		            if(transaction.getProductsJson() != null)
		            	element.put("products", transaction.getProducts());
		            transactionDetails.add(element);
		        }
		        
		        return new ResponseEntity<>(transactionDetails, HttpStatus.OK);
		    }
		}
		return new ResponseEntity<>("No Details", HttpStatus.OK);
	}
	
	@Cacheable(value = "transactions", key = "#transaction.entityKey")
	public ResponseEntity<?> getTransactionByEntityKey(Character entityKey){
		Long userId = jwtAuthenticationFilter.getLoggedInUserId(); 
		if(userId != null) {
			List<Transaction> transactions = transactionRepo.findByEntityKey(entityKey, userId);
			if (transactions != null && !transactions.isEmpty()) {
		        List<Map<String, Object>> transactionDetails = new ArrayList<>();
		        for (Transaction transaction : transactions) {
		            Map<String, Object> element = new HashMap<>();
		            element.put("id", transaction.getId());
		            element.put("amount", transaction.getAmount());
		            element.put("dateOfTransaction", transaction.getDateOfTransaction());
		            element.put("discount", transaction.getDiscount());
		            element.put("entityId", transaction.getEntityId());
		            element.put("entityKey", transaction.getEntityKey());
		            element.put("mode", transaction.getMode());
		            element.put("transaction", transaction.getTransaction());
		            if(transaction.getProductsJson() != null)
		            	element.put("products", transaction.getProducts());
		            transactionDetails.add(element);
		        }
		        
		        return new ResponseEntity<>(transactionDetails, HttpStatus.OK);
		    }
		}
		return new ResponseEntity<>("No Details", HttpStatus.OK);
	}
	
	public ResponseEntity<?> getSaleReport(Map<String, Object> params) {

	    Long createdBy = jwtAuthenticationFilter.getLoggedInUserId();
	    String description = "sale";

	    Long entityId = Long.valueOf(params.get("entityId").toString());
	    String filter = params.get("filter").toString();

	    LocalDate startDate = LocalDate.parse(params.get("startDate").toString());
	    LocalDate endDate = LocalDate.parse(params.get("endDate").toString());

	    int offset = Integer.parseInt(params.get("offset").toString());
	    int limit = Integer.parseInt(params.get("limit").toString());

	    int page = offset / limit;

	    Pageable pageable = PageRequest.of(page, limit, Sort.by("dateOfTransaction").descending().and(Sort.by("id").descending()));

	    Page<Transaction> pageData;
	    Long totalAmount;

	    if (filter.equals("all") || entityId == 0) {

	        pageData = transactionRepo.findByCreatedByAndDescriptionAndDateOfTransactionBetween(
	                createdBy, description, startDate, endDate, pageable);

	        totalAmount = Optional.ofNullable(transactionRepo.getTotalAmountAll(
	                createdBy, description, startDate, endDate)).orElse(0L);

	    } else {

	        pageData = transactionRepo.findByCreatedByAndDescriptionAndEntityIdAndDateOfTransactionBetween(
	                createdBy, description, entityId, startDate, endDate, pageable);
	        totalAmount = Optional.ofNullable(
	                transactionRepo.getTotalAmountParty(createdBy, description, entityId, startDate, endDate)
	        ).orElse(0L);
	    }

	    Long totalCount = pageData.getTotalElements();

	    Map<String, Object> response = new HashMap<>();
	    response.put("saleTransactions", pageData.getContent());
	    response.put("totalSaleAmount", totalAmount);
	    response.put("totalSaleCount", totalCount);

	    return ResponseEntity.ok(response);
	}
	
	public ApiResponse<?> home(Long userId){
    	DashboardSummaryDTO todayDashboardSummary = transactionRepo.getTodayDashboardSummary(userId);
    	return ApiResponse.builder()
	            .success(true)
	            .message("User Dashboard")
	            .data(todayDashboardSummary)
	            .timestamp(LocalDateTime.now())
	            .build();
    }
	
}
