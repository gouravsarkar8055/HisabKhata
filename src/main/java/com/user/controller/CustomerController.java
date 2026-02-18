package com.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.entity.Customer;
import com.user.entity.SellerProduct;
import com.user.entity.Transaction;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.service.CustomerServiceImpl;

@RestController
@RequestMapping("/cust")
public class CustomerController {

	@Autowired
	private CustomerServiceImpl customerService;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@PostMapping("/addCust")
	public ResponseEntity<?> addCust(@RequestBody Customer cust){
		ModelMap m = new ModelMap();
		String addEmp = customerService.addCust(cust);
		m.addAttribute("status", addEmp);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@PutMapping("/updateCust")
	public ResponseEntity<?> updateCust(@RequestBody Customer cust){
		ModelMap m = new ModelMap();
		String addEmp = customerService.updateCust(cust);
		m.addAttribute("status", addEmp);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteCust")
	public ResponseEntity<?> deleteCust(@RequestParam(name="id") Long id){
		ModelMap m = new ModelMap();
		String delete = customerService.deleteCust(id);
		m.addAttribute("status", delete);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@GetMapping("/getAllCust")
	public ResponseEntity<?> getAllCust(){
		return customerService.getAllCust();
	}
	
	@GetMapping("/getCustById")
	public ResponseEntity<?> getCustById(@RequestParam(name="id") Long id){
		return customerService.getCustById(id);
	}
	
	@PostMapping("/add_cust_transaction")
	public ResponseEntity<?> addTransaction(@RequestBody Transaction custTransaction){
		ModelMap m = new ModelMap();
		Long transaction = customerService.addTransaction(custTransaction);
		if(transaction==null)
			m.addAttribute("status", "Transaction Failed");
		else
			m.addAttribute("status", "Transaction Success");
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@GetMapping("/get_cust_transactions")
	public ResponseEntity<?> getCustTransactions(){
		ModelMap m = new ModelMap();
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		ResponseEntity<?> custTransactions = customerService.getCustTransactions(userId, null);
		if(custTransactions == null)
			return new ResponseEntity<>(m.addAttribute("status", "No Transactions"),HttpStatus.OK);
			
		else
			return new ResponseEntity<>(custTransactions,HttpStatus.OK);
	}
	
	@GetMapping("/get_custTransaction")
	public ResponseEntity<?> getTransactionByCustId(@RequestParam(name = "id") Long id){
		ModelMap m = new ModelMap();
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		ResponseEntity<?> custTransactions = customerService.getCustTransactions(userId, id);
		if(custTransactions == null)
			return new ResponseEntity<>(m.addAttribute("status", "No Transactions"),HttpStatus.OK);
		else
			return new ResponseEntity<>(custTransactions,HttpStatus.OK);
	}
	
	@PostMapping("/addProduct")
	public ResponseEntity<?> addProd(@RequestBody SellerProduct sellerProds, @RequestParam(name = "custId") Long custId){
		ModelMap m = new ModelMap();
		String addProd = customerService.addProd(sellerProds, custId);
		m.addAttribute("status", addProd);
		return new ResponseEntity<>(m, HttpStatus.OK);
	}
	
	@PutMapping("/updateProducts")
	public ResponseEntity<?> updateProducts(@RequestBody SellerProduct sellerProds, @RequestParam(name = "custId") Long custId){
		ModelMap m = new ModelMap();
		String editProd = customerService.updateProducts(sellerProds, custId);
		m.addAttribute("status", editProd);
		return new ResponseEntity<>(m, HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteProduct")
	public ResponseEntity<?> deleteProduct(@RequestParam(name="id") Long id){
		ModelMap m = new ModelMap();
		String delete = customerService.deleteProd(id);
		m.addAttribute("status", delete);
		return new ResponseEntity<>(delete,HttpStatus.OK);
	}
	
	@GetMapping("/searchCust")
	public ResponseEntity<?> searchCust(@RequestParam(name="name") String name){
		return customerService.searchCust(name);
	}
}