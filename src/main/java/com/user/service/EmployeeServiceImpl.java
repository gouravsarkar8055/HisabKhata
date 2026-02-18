package com.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.user.entity.EmpTransaction;
import com.user.entity.Employee;
import com.user.entity.Transaction;
import com.user.jwtauthentication.JwtAuthenticationFilter;
import com.user.repository.EmpTransactionRepo;
import com.user.repository.EmployeeRepo;
import com.user.repository.TransactionRepo;

import jakarta.transaction.Transactional;

@Service
public class EmployeeServiceImpl {
	
	@Autowired
	private EmployeeRepo employeeRepo;
	
	@Autowired
	private TransactionRepo transactionRepo;
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Autowired
	private EmpTransactionRepo empTtansactionRepo;
	
	public String addEmp(@RequestBody Employee emp){
		Optional<Employee> findById = employeeRepo.findById(emp.getId());
		Employee findByEmail = employeeRepo.findByEmail(emp.getEmail());
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		if(userId != null) {
		if(findById == null && findByEmail == null) {
			emp.setCreatedBy(userId);
			employeeRepo.save(emp);
			return "Employee Added Successfully";
		}
		else
			return "Employee Already Available";
		}
		return "Login UserId is Empty or Null";
	}
	
	public ResponseEntity<String> updateEmp(@RequestBody Employee emp){
		return employeeRepo.findById(emp.getId())
	            .map(existing -> {
	                existing.setName(emp.getName());
	                existing.setAddress(emp.getAddress());
	                existing.setPhone(emp.getPhone());
	                existing.setSal(emp.getSal());
	                existing.setDateOfJoin(emp.getDateOfJoin());
	                employeeRepo.save(existing);
	                return new ResponseEntity<>("Employee Updated Successfully", HttpStatus.OK);
	            })
	            .orElse(new ResponseEntity<>("Employee Not Found", HttpStatus.NOT_FOUND));
	}
	
	@Transactional
	public String deleteEmp(@RequestParam Long id){

		Optional<Employee> findById = employeeRepo.findById(id);
		System.out.println(findById.toString());
		if(findById != null) {
			try {
				
				 employeeRepo.deleteById(id);
				 
				return "Employee "+id+" has been deleted";
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "something wrong";
	}
	public ResponseEntity<?> getEmpById(@RequestParam Long id){		
		Optional<Employee> findById = employeeRepo.findById(id);
		return new ResponseEntity<>(findById,HttpStatus.OK);
	}
	
	public String addTransaction(@RequestBody Transaction empTransaction) {
		Optional<Employee> findById = employeeRepo.findById(empTransaction.getEntityId());
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
		if(findById != null) {
			empTransaction.setCreatedBy(userId);
			empTransaction.setEntityKey('E');
			transactionRepo.save(empTransaction);
			return "Transaction Success";
		}else
			return "Employee Not Available";
	}
	
	public ResponseEntity<?> getAllEmpDetails() {   
		Long userId = jwtAuthenticationFilter.getLoggedInUserId();
	    List<Employee> employees = employeeRepo.findByCreatedBy(userId);
	    
	    if (employees != null && !employees.isEmpty()) {
	        List<Map<String, Object>> empDetails = new ArrayList<>();
	        for (Employee employee : employees) {
	            Map<String, Object> element = new HashMap<>();
	            element.put("id", employee.getId());
	            element.put("email", employee.getEmail());
	            element.put("createdBy", employee.getCreatedBy());
	            element.put("name", employee.getName());
	            element.put("address", employee.getAddress());
	            element.put("dateOfJoin", employee.getDateOfJoin());
	            element.put("phone", employee.getPhone());
	            element.put("sal", employee.getSal());
	            empDetails.add(element);
	        }
	        
	        return new ResponseEntity<>(empDetails, HttpStatus.OK);
	    }
	    
	    return new ResponseEntity<>("No Details", HttpStatus.OK);
	}
	
	public List<Map<String, Object>> getEmpTransactions(@RequestParam Long userId, @RequestParam Long id){
		
		List<EmpTransaction> transactionsByEmpId = null;
			if(id == null)
				transactionsByEmpId = empTtansactionRepo.findTransactionsByCreatedBy(userId);
			else
				transactionsByEmpId = empTtansactionRepo.findTransactionsByEmpId(userId, id);
		
		if(transactionsByEmpId != null && !transactionsByEmpId.isEmpty()) {
			List<Map<String, Object>> transactionDetails = new ArrayList<>();
			
			for(EmpTransaction transactions : transactionsByEmpId) {
				Map<String, Object> element = new HashMap<>();
				element.put("id", transactions.getId());
				element.put("amount", transactions.getAmount());
				element.put("dateOfTransaction", transactions.getDateOfTransaction());
				element.put("employeeId", transactions.getEmpId());
				element.put("mode", transactions.getMode());
				element.put("createdBy", transactions.getCreatedBy());
				element.put("transaction", transactions.getTransaction());
				transactionDetails.add(element);
			}
			return transactionDetails;
		}
		return null;
	}

}