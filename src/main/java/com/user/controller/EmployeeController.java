package com.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.entity.EmpTransaction;
import com.user.entity.Employee;
import com.user.entity.Transaction;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.service.EmployeeServiceImpl;

@RestController
@RequestMapping("/emp")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {
	
	@Autowired
	private EmployeeServiceImpl employeeService;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@PostMapping("/addEmp")
	public ResponseEntity<?> addEmp(@RequestBody Employee emp){
		String addEmp = employeeService.addEmp(emp);
		ModelMap m = new ModelMap();
		m.addAttribute("status", addEmp);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@PutMapping("/updateEmp")
	public ResponseEntity<?> updateEmp(@RequestBody Employee emp){
		ResponseEntity<String> updateEmp = employeeService.updateEmp(emp);
		ModelMap m = new ModelMap();
		m.addAttribute("status", updateEmp);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@DeleteMapping("/deleteEmp")
	public ResponseEntity<?> deleteEmp(@RequestParam(name = "id") Long id){
		System.out.println(id);
		String delete = employeeService.deleteEmp(id);
		ModelMap m = new ModelMap();
		m.addAttribute("status", delete);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@GetMapping("/getAllEmpDetails")
	public ResponseEntity<?> getAllEmpDetails(){
		return employeeService.getAllEmpDetails();
	}
	
	@GetMapping("/getEmpById")
	public ResponseEntity<?> getEmpByMail(@RequestParam(name = "id") Long id){
		return employeeService.getEmpById(id);
	}
	
	@PostMapping("/transaction")
	public ResponseEntity<?> addTransaction(@RequestBody Transaction empTransaction){
		ModelMap m = new ModelMap();
		String transaction = employeeService.addTransaction(empTransaction);
		m.addAttribute("status", transaction);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@GetMapping("/get_transaction")
	public ResponseEntity<?> getEmpTransactions(){
		ModelMap m = new ModelMap();
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		List<Map<String, Object>> empTransactions = employeeService.getEmpTransactions(userId, null);
		if(empTransactions == null)
			m.addAttribute("status", "No Transactions");
		else
			m.addAttribute("transactions", empTransactions);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
	@GetMapping("/get_empTransaction")
	public ResponseEntity<?> getTransactionByEmpId(@RequestParam(name = "id") Long id){
		ModelMap m = new ModelMap();
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		List<Map<String, Object>> empTransactions = employeeService.getEmpTransactions(userId, id);
		if(empTransactions == null)
			m.addAttribute("status", "No Transactions");
		else
			m.addAttribute("transactions", empTransactions);
		return new ResponseEntity<>(m,HttpStatus.OK);
	}
	
}
