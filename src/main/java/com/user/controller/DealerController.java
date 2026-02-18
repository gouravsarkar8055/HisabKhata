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

import com.user.entity.BuyerProduct;
import com.user.entity.Dealer;
import com.user.entity.DealerTransaction;
import com.user.service.DealerServiceImpl;

@RestController
@RequestMapping("/dlr")
public class DealerController {

	@Autowired
	private DealerServiceImpl dealerService;
	
	@PostMapping("/addDealer")
	public ResponseEntity<?> addDlr(@RequestBody Dealer dlr){
		String addDlr = dealerService.addDlr(dlr);
		ModelMap m = new ModelMap();
		m.addAttribute("status", addDlr);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@PutMapping("/updateDealer")
	public ResponseEntity<?> updateDlr(@RequestBody Dealer dlr){
		String updateDlr = dealerService.updateDlr(dlr);
		ModelMap m = new ModelMap();
		m.addAttribute("status", updateDlr);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteDealer")
	public ResponseEntity<?> deleteDlr(@RequestParam(name="id") Long id){
		System.out.println(id);
		String delete = dealerService.deleteDlr(id);
		ModelMap m = new ModelMap();
		m.addAttribute("status", delete);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@GetMapping("/getAllDealer")
	public ResponseEntity<?> getAllDlr(){
		return dealerService.getAllDlr();
	}
	
	@GetMapping("/getDealerByCode")
	public ResponseEntity<?> getDlrByCode(@RequestParam(name="id") Long id){
		return dealerService.getDlrByCode(id);
	}
	
	@PostMapping("/addProduct")
	public ResponseEntity<?> addProd(@RequestBody BuyerProduct buyerProd, @RequestParam(name="dlrId") Long dlrId){
		String addProd = dealerService.addProd(buyerProd, dlrId);
		return new ResponseEntity<>(addProd, HttpStatus.OK);
	}
	
	@PutMapping("/editProduct")
	public ResponseEntity<?> editProd(@RequestBody BuyerProduct buyerProd,@RequestParam(name="id") Long id){
		String editProd = dealerService.editProd(buyerProd, id);
		return new ResponseEntity<>(editProd, HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteProduct")
	public ResponseEntity<?> deleteProduct(@RequestParam(name="id") Long id){
		String delete = dealerService.deleteProduct(id);
		return new ResponseEntity<>(delete,HttpStatus.OK);
	}
	
	@GetMapping("/getTtansactionByCode")
	public List<DealerTransaction> getTransactionByCode(@RequestParam(name="id") Long id){
		return dealerService.transactionList(id);
	}
	
}
