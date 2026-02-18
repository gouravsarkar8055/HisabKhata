package com.user.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.user.entity.BuyerProduct;
import com.user.entity.Dealer;
import com.user.entity.DealerTransaction;
import com.user.entity.Transaction;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.repository.BuyerProductRepo;
import com.user.repository.DealerRepo;
import com.user.repository.DlrTransactionRepo;
import com.user.repository.TransactionRepo;

import jakarta.transaction.Transactional;

@Service
public class DealerServiceImpl {
	
	@Autowired
	private DealerRepo dealerRepo;
	@Autowired
	private BuyerProductRepo buyerProductRepo;
	@Autowired
	private DlrTransactionRepo dlrTransactionRepo;
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private TransactionRepo transactionRepo;
	
	public String addDlr(@RequestBody Dealer dlr){
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    String email = dlr.getEmail();
	    Dealer dealer = null;
	    
	    if (email != null) {
	    	dealer = dealerRepo.findByEmail(email); 
	    }
	    
	    if (dealer == null) {
	    	dlr.setCreatedBy(userId);
	    	dealerRepo.save(dlr);
	        return "Dealer Added Successfully";
	    }
	    return "Dealer Already Available";
	}
	
	public String updateDlr(@RequestBody Dealer dlr){
		Optional<Dealer> findById = dealerRepo.findById(dlr.getId());
		
		if(findById.isPresent()) {
			Dealer existingDealer = findById.get();
			existingDealer.setName(dlr.getName());
			existingDealer.setAddress(dlr.getAddress());
			existingDealer.setPhno(dlr.getPhno());
			dealerRepo.save(existingDealer);
			return "Dealer Updated Successfully";
		}
		return "Dealer Not Found";
	}
	
	@Transactional
	public String deleteDlr(@RequestParam Long id){

		Optional<Dealer> findById = dealerRepo.findById(id);
		System.out.println(findById.toString());
		if(findById != null) {
			try {
				
				dealerRepo.deleteById(id);
				 
				return "Dealer "+id+" has been deleted";
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "something wrong";
	}
	
	public ResponseEntity<?> getAllDlr(){		
		List<Dealer> findAll = dealerRepo.findAll();
		return new ResponseEntity<>(findAll,HttpStatus.OK);
	}
	
	
	public ResponseEntity<?> getDlrByCode(@RequestParam Long id){		
		Optional<Dealer> findByCode = dealerRepo.findById(id);
		return new ResponseEntity<>(findByCode,HttpStatus.OK);
	}
	
	public Long addTransaction(@RequestBody Transaction dlrTransaction) {
		Optional<Dealer> findById = dealerRepo.findById(dlrTransaction.getEntityId());
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		if(findById.isPresent()) {
			dlrTransaction.setCreatedBy(userId);
			dlrTransaction.setEntityKey('D');
			Transaction savedTransaction = transactionRepo.save(dlrTransaction);
			return savedTransaction.getId();
		}else
			return null;
	}
	
	public String addProd(@RequestBody BuyerProduct buyerProduct, @RequestParam Long dlrId) {
	    Optional<Dealer> findByCustId = dealerRepo.findById(dlrId);
	    Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    if (!findByCustId.isPresent()) {
	        return "Dealer Not Found";
	    }

	    // Validate input
	    if (buyerProduct.getProducts() == null || buyerProduct.getProducts().isEmpty()) {
	        throw new IllegalArgumentException("Products cannot be empty");
	    }

	    // Save transaction details
	    Transaction ct = new Transaction();
	    ct.setDateOfTransaction(buyerProduct.getDateOfBuy());
	    ct.setAmount(buyerProduct.getAmount());
	    ct.setDiscount(buyerProduct.getDiscount());
	    ct.setMode("product");
	    ct.setDescription("purchase");
	    ct.setEntityId(dlrId);

	    Long transactionId = addTransaction(ct); // Ensure this method handles exceptions
	    if(transactionId == null)
	    	return "Transaction Failed";
	    
	    // Convert product list to JSON before saving
	    buyerProduct.setProducts(buyerProduct.getProducts());
	    buyerProduct.setCreatedBy(userId);
	    buyerProduct.setDlrId(dlrId);
	    // Save seller product with JSON data
	    buyerProduct.setTransactionId(transactionId);
	    BuyerProduct savedProduct = buyerProductRepo.save(buyerProduct);

	    return savedProduct != null ? "Products added successfully" : "Something went wrong";
	}
	
	public String editProd(@RequestBody BuyerProduct buyerProd, @RequestParam Long id){
		@SuppressWarnings("deprecation")
		BuyerProduct findById = buyerProductRepo.getById(id);
		
		
		return "Product Not Found";
	}
	
	@Transactional
	public String deleteProduct(@RequestParam Long id){
		@SuppressWarnings("deprecation")
		BuyerProduct findById = buyerProductRepo.getById(id);
		if(findById != null) {
			try {
				buyerProductRepo.delete(findById);
				return "Product has been deleted";
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "something wrong";
	}
	
	public String addTransaction(DealerTransaction dealerTransaction, Long id) {
		Optional<Dealer> findById = dealerRepo.findById(id);
		if(findById != null) {
			dlrTransactionRepo.save(dealerTransaction);
			return "Transaction Success";
		}else
			return "Dealer Not Available";
	}
	
	public List<DealerTransaction> transactionList(Long id){
		return dlrTransactionRepo.findTransactionsByDlrId(id);
	}
}