package com.user.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.user.entity.Customer;
import com.user.entity.CustomerTransaction;
import com.user.entity.SellerProduct;
import com.user.entity.Transaction;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.repository.CustomerRepo;
import com.user.repository.CustomerTransactionRepo;
import com.user.repository.SellerProductRepo;
import com.user.repository.TransactionRepo;

import jakarta.transaction.Transactional;

@Service
public class CustomerServiceImpl {
	
	@Autowired
	private CustomerRepo customerRepo;
	
	@Autowired
	private TransactionRepo transactionRepo;
	
	@Autowired
	private CustomerTransactionRepo customerTransactionRepo;
	
	@Autowired
	private SellerProductRepo sellerProductRepo;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Autowired
	private TransactionServiceImpl transactionServiceImpl;
	
	public String addCust(@RequestBody Customer cust) {
	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    String email = cust.getEmail();
	    Customer customer = null;
	    
	    if (email != null) {
	    	customer = customerRepo.findByEmail(email); 
	    }
	    
	    if (customer == null) {
	    	cust.setCreatedBy(userId);
	        Customer newCustomer = customerRepo.save(cust);
//	        Transaction transaction = new Transaction();
//	        transaction.setDateOfTransaction(cust.getDateOfLastTransaction());
//	        transaction.setEntityId(newCustomer.getId());
//	        transaction.setBalance(newCustomer.getBalanceAmount());
//	        transaction.setEntityKey('C');
//	        transactionServiceImpl.addTransaction(transaction);
	        return "Customer Added Successfully";
	    }
	    return "Customer Already Available";
	}
	
	public String updateCust(@RequestBody Customer cust) {
	    Optional<Customer> findById = customerRepo.findById(cust.getId());

	    if (findById.isPresent()) { 
	        Customer existingCustomer = findById.get(); 
	        existingCustomer.setName(cust.getName()); 
	        existingCustomer.setEmail(cust.getEmail());
	        existingCustomer.setAddress(cust.getAddress()); 
	        existingCustomer.setBalanceAmount(cust.getBalanceAmount());
	        existingCustomer.setPhno(cust.getPhno()); 
	        customerRepo.save(existingCustomer); 
	        return "Customer Updated Successfully";
	    }
	    return "Customer Not Found";
	}

	
	@Transactional
	public String deleteCust(@RequestParam Long id){

		Optional<Customer> findByCode = customerRepo.findById(id);
		System.out.println(findByCode.toString());
		if(findByCode.isPresent()) {
			try {
				
				customerRepo.deleteById(id);
				 
				return "Customer "+id+" has been deleted";
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "something wrong";
	}
	
	public ResponseEntity<?> getAllCust(){	
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		List<Customer> findAll = customerRepo.findByCreatedBy(userId);
		return new ResponseEntity<>(findAll,HttpStatus.OK);
	}
	
	
	public ResponseEntity<?> getCustById(@RequestParam Long id){		
		Optional<Customer> findByCode = customerRepo.findById(id);
		return new ResponseEntity<>(findByCode,HttpStatus.OK);
	}
	
	public Long addTransaction(@RequestBody Transaction custTransaction) {
		Optional<Customer> findById = customerRepo.findById(custTransaction.getEntityId());
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		if(findById.isPresent()) {
			custTransaction.setCreatedBy(userId);
			custTransaction.setEntityKey('C');
			Transaction savedTransaction = transactionRepo.save(custTransaction);
			return savedTransaction.getId();
		}else
			return null;
	}
	
public ResponseEntity<?> getCustTransactions(@RequestParam Long userId, @RequestParam Long id){
		
		List<CustomerTransaction> transactionsByCustId = null;
			if(id == null)
				transactionsByCustId = customerTransactionRepo.findByCreatedBy(userId);
			else
				transactionsByCustId = customerTransactionRepo.findTransactionsByEmpId(userId, id);
		
			return (ResponseEntity<?>) transactionsByCustId;
		
	}
public String addProd(@RequestBody SellerProduct sellerProduct, @RequestParam Long custId) {
    Optional<Customer> findByCustId = customerRepo.findById(custId);
    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
    if (!findByCustId.isPresent()) {
        return "Customer Not Found";
    }

    // Validate input
    if (sellerProduct.getProducts() == null || sellerProduct.getProducts().isEmpty()) {
        throw new IllegalArgumentException("Products cannot be empty");
    }
    // Save transaction details
    Transaction ct = new Transaction();
    ct.setDateOfTransaction(sellerProduct.getDateOfSell());
    ct.setAmount(sellerProduct.getAmount());
    ct.setDiscount(sellerProduct.getDiscount());
    ct.setMode("product");
    ct.setDescription("sale");
    ct.setEntityKey('C');
    ct.setEntityId(custId);
    Long transactionId = 1L; // Ensure this method handles exceptions
    if(transactionId == null)
    	return "Transaction Failed";
    // Convert product list to JSON before saving
    sellerProduct.setProducts(sellerProduct.getProducts());
    sellerProduct.setCreatedBy(userId);
    sellerProduct.setCustId(custId);
    // Save seller product with JSON data
    sellerProduct.setTransactionId(transactionId);
    SellerProduct savedProduct = sellerProductRepo.save(sellerProduct);

    return savedProduct != null ? "Products added successfully" : "Something went wrong";
}


public String updateProducts(@RequestBody SellerProduct sellerProds, @RequestParam Long custId) {
    Optional<Customer> findByCustId = customerRepo.findById(custId);
    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
    Optional<SellerProduct> sellerById = sellerProductRepo.findById(sellerProds.getId());
    if (findByCustId.isPresent()) {
    	// Validate input
        if (sellerProds.getProducts() == null || sellerProds.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Products cannot be empty");
        }
     // Convert product list to JSON before saving
        if(sellerById != null) {
        sellerById.get().setProducts(sellerProds.getProducts());
        sellerById.get().setCreatedBy(userId);
        sellerById.get().setCustId(custId);
        sellerById.get().setAmount(sellerProds.getAmount());
        
        // Save seller product with JSON data
        SellerProduct updateProduct = sellerProductRepo.save(sellerById.get());
        
       // transactionServiceImpl.editTransaction(null);
        return "Products updated or added successfully";
        }
        else {
        	return "Products Details not found";
        }
    }
    
    return "Customer Not Found";
}


public String deleteProd(@RequestParam Long id) {
	Optional<SellerProduct> findById = sellerProductRepo.findById(id);
	if(findById.isPresent()) {
		try {
			
			sellerProductRepo.deleteById(id);
			 
			return "Customer "+id+" has been deleted";
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	return "something wrong";
}

public ResponseEntity<?> searchCust(String name){	
	Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	List<Customer> findAll = customerRepo.findByCustomerName(userId, name);
	return new ResponseEntity<>(findAll,HttpStatus.OK);
}
}
