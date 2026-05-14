package com.user.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.user.api_response.ApiResponse;
import com.user.dto.AttendanceDTO;
import com.user.entity.Attendance;
import com.user.entity.BusinessEntity;
import com.user.entity.PartyDetails;
import com.user.entity.Staff;
import com.user.entity.TempUser;
import com.user.entity.Transaction;
import com.user.entity.UserEntity;
import com.user.jwtauthentication.JwtKeyManager;
import com.user.jwtauthentication.JwtUtil;
import com.user.repository.StaffRepository;
import com.user.service.BusinessServiceImpl;
import com.user.service.PartyServiceImpl;
import com.user.service.TransactionServiceImpl;
import com.user.service.UserServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {
	
	private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    public UserController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

	@Autowired
	private UserServiceImpl userServiceImpl;
	
	@Autowired
	private PartyServiceImpl partyServiceImpl;
	
	@Autowired
	private StaffRepository staffRepo;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private TransactionServiceImpl transactionServiceImpl;
	
	@Autowired
    private BusinessServiceImpl businessServiceImpl;
	
	@GetMapping("/test-mail")
	public ResponseEntity<?> testMail(@RequestParam(name="email") String email) {
		String res = userServiceImpl.signinLoginWithEmail(email);
		ModelMap m = new ModelMap();
		m.addAttribute("status", res);
		return new ResponseEntity<>(m, HttpStatus.OK);
	}
	
	@GetMapping("/getUserData")
	public ResponseEntity<?> getUserData() {
		ApiResponse<UserEntity> res = userServiceImpl.getUserData();
		
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@GetMapping("/home")
	public ResponseEntity<?> home(@RequestParam("userId") Long userId) {
		ApiResponse<?> res = transactionServiceImpl.home(userId);
		
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
	
	@PutMapping("/updateUserData")
	public ResponseEntity<?> updateUserData(@RequestParam("id") Long id,
            @RequestBody UserEntity user) {
		ApiResponse<UserEntity> res = userServiceImpl.updateUserData(id, user);
		
		return new ResponseEntity<>(res, HttpStatus.OK);
	}


	@PostMapping("/addTempUser")
	public ResponseEntity<?> addTempUser(@RequestBody TempUser temp){
		String res = userServiceImpl.addTempUser(temp);
		ModelMap m = new ModelMap();
		m.addAttribute("status", res);
		return new ResponseEntity<>(m, HttpStatus.OK);
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestParam(name="email") String email, @RequestParam(name="password") String password) {
	    if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
	        return ResponseEntity.badRequest().body(Map.of("status", "Error", "message", "Email and password must not be empty"));
	    }

	    try {
	        Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(email, password));

	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	        String authToken = jwtUtil.generateToken(userDetails.getUsername());

	        return ResponseEntity.ok(Map.of(
	                "authentication", authToken,
	                "status", "Login Successful"
	        ));
	    } catch (BadCredentialsException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "Error", "message", "Invalid email or password"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "Error", "message", "An unexpected error occurred"));
	    }
	}
	
	@PostMapping("/logout")
    public ResponseEntity<String> logout() {
        JwtKeyManager.rotateSecretKey(); // 🔄 Rotate secret key to invalidate all tokens
        return ResponseEntity.ok("Logged out successfully. All tokens are now invalid.");
    }

	@GetMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(
	        @RequestParam(name = "email") String email,
	        @RequestParam(name = "otp") String otp) {

	    try {
	        // 1. Verify OTP
	        String isVerified = userServiceImpl.verifyEmailOtp(email, otp);

	        if (!"verified".equalsIgnoreCase(isVerified)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
	                    Map.of("status", "Error", "message", "Invalid or expired OTP")
	            );
	        }

	        // 2. Load user
	        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

	        // 3. Manually authenticate user (NO PASSWORD)
	        Authentication authentication = new UsernamePasswordAuthenticationToken(
	                userDetails, null, userDetails.getAuthorities());

	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        // 4. Generate JWT token
	        String authToken = jwtUtil.generateToken(userDetails.getUsername());

	        // 5. Return response
	        return ResponseEntity.ok(Map.of(
	                "authentication", authToken,
	                "status", "Login Successful"
	        ));

	    } catch (UsernameNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
	                Map.of("status", "Error", "message", "User not found")
	        );
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
	                Map.of("status", "Error", "message", "An unexpected error occurred")
	        );
	    }
	}
	
	@GetMapping("/resendOtp")
	public ResponseEntity<?> resendOtp(@RequestParam(name="email") String email) {
		String isVerified = userServiceImpl.resendOtp(email);
		ModelMap m = new ModelMap();
		m.addAttribute("status", isVerified);
		return new ResponseEntity<>(m, HttpStatus.OK);
	}
	
	@PostMapping("/sendSms")
    public String sendSms(@RequestParam("to") String to, @RequestParam("message") String message) {
		userServiceImpl.sendSms(to, message);
        return "SMS sent to " + to;
    }
	
//	@GetMapping("/getAllTransactions")
//	public ResponseEntity<?> getAllTransactions(){
//		return transactionServiceImpl.getAllTransactions();
//	}
	
	@GetMapping("/getAllTransactions")
	public ResponseEntity<?> getAllTransactions(
	        @RequestParam("page") int page,
	        @RequestParam("size") int size) {
	    return transactionServiceImpl.getAllTransactions(page, size);
	}
	
	@GetMapping("/getTransactionByEntityKeyAndId")
	public ResponseEntity<?> getTransactionByEntityKeyAndId(@RequestParam("entityKey") Character entityKey, @RequestParam("entityId") Long entityId){
		return transactionServiceImpl.getTransactionByEntityKeyAndId(entityKey, entityId);
	}
	
	@GetMapping("/getTransactionByEntityKey")
	public ResponseEntity<?> getTransactionByEntityKey(@RequestParam("entityKey") Character entityKey){
		return transactionServiceImpl.getTransactionByEntityKey(entityKey);
	}
	
	@GetMapping("/getTransactionDetailById")
	public ResponseEntity<?> getProductDetailsByTransactionId(@RequestParam(name="transactionId") Long transactionId){
		return transactionServiceImpl.getTransactionDetailById(transactionId);
	}
	
	@PostMapping("/addTransaction")
	public ResponseEntity<ApiResponse<Transaction>> addTransaction(@RequestBody Transaction transaction){
		 ApiResponse<Transaction> response = transactionServiceImpl.addTransaction(transaction);
		if (response.isSuccess()) {
	        return ResponseEntity.ok(response); // 200 OK
	    }

	    // Validation errors return HTTP 400
	    return ResponseEntity.badRequest().body(response);
	}
	
	@PutMapping("/editTransaction")
	public ResponseEntity<ApiResponse<?>> editTransaction(@RequestBody Transaction transaction) {

		ApiResponse<Transaction> updated = transactionServiceImpl.editTransaction(transaction);

	    if (updated.isSuccess()) {
	        return ResponseEntity.ok(updated); // 200 OK
	    }

	    // Validation errors return HTTP 400
	    return ResponseEntity.badRequest().body(updated);
	}
	
	@DeleteMapping("/deleteTransaction")
	public ResponseEntity<ApiResponse<?>> deleteTransaction(@RequestParam("id") Long id) {

        ApiResponse<?> response = transactionServiceImpl.deleteTransaction(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
	
	@GetMapping("/getAllParties")
    public ResponseEntity<ApiResponse<Object>> getAllParties(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("direction") String direction) {

        return partyServiceImpl.getAllParties(page, size, sortBy, direction, 'A');
    }
	
	@PostMapping("/addParty")
	public ResponseEntity<ApiResponse<PartyDetails>> addParty(
	        @RequestBody PartyDetails party) {

	    ApiResponse<PartyDetails> response = partyServiceImpl.addParty(party);

	    if (response.isSuccess()) {
	        return ResponseEntity.ok(response); // 200 OK
	    }

	    // Validation errors return HTTP 400
	    return ResponseEntity.badRequest().body(response);
	}
	
	@PutMapping("/updateParty")
	public ResponseEntity<ApiResponse<PartyDetails>> updateParty(
	        @RequestBody PartyDetails party) {

	    ApiResponse<PartyDetails> response = partyServiceImpl.updateParty(party);

	    if (response.isSuccess()) {
	        return ResponseEntity.ok(response);
	    }
	    return ResponseEntity.badRequest().body(response);
	}
	
	@DeleteMapping("/deleteParty")
	public ResponseEntity<ApiResponse<?>> deleteParty(@RequestParam("partyId") Long partyId) {
	    return partyServiceImpl.deleteParty(partyId);
	}
	
	@GetMapping("/getAllPartiesName")
	public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllPartiesName(){
		return partyServiceImpl.getAllPartiesName();
	}
	
	@GetMapping("/getPartyById")
	public ResponseEntity<ApiResponse<PartyDetails>> getPartyById(@RequestParam("partyId") Long partyId){
		return partyServiceImpl.getPartyById(partyId);
	}
	
	@GetMapping("/getPartiesByKey")
    public ResponseEntity<ApiResponse<Object>> getPartiesByKey(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("direction") String direction,
            @RequestParam("key") Character key) {

        return partyServiceImpl.getAllParties(page, size, sortBy, direction, key);
    }
	
	@GetMapping("/getPartyByName")
	public ResponseEntity<ApiResponse<PartyDetails>> getPartyByName(@RequestParam("partyName") String partyName){
		return partyServiceImpl.getPartyByName(partyName);
	}
	
	@PostMapping("/getSaleReport")
	public ResponseEntity<?> getSaleReport(@RequestBody Map<String, Object> params) {
	    ResponseEntity<?> response = transactionServiceImpl.getSaleReport(params);

	    Object body = response.getBody();

	    if (body instanceof List<?>) {
	        System.out.println("Returned count = " + ((List<?>) body).size());
	    }
	    return response;
	}
	
	@PostMapping("/addBusiness")
    public BusinessEntity addBusiness(@RequestBody BusinessEntity business) {
        return businessServiceImpl.addBusiness(business);
    }

    @PutMapping("/updateBusiness")
    public BusinessEntity updateBusiness(@RequestParam("id") Long id,
                                         @RequestBody BusinessEntity business) {
        return businessServiceImpl.updateBusiness(id, business);
    }

    @DeleteMapping("/deleteBusiness")
    public String deleteBusiness(@RequestParam("id") Long id) {
    	businessServiceImpl.deleteBusiness(id);
        return "Deleted successfully";
    }

    @GetMapping("/getBusinessById")
    public BusinessEntity getBusiness(@RequestParam("id") Long id) {
        return businessServiceImpl.getBusinessById(id);
    }

    @GetMapping("/getAllBusiness")
    public List<BusinessEntity> getAllBusinesses() {
        return businessServiceImpl.getAllBusinesses();
    }

    @GetMapping("/getBusinessByUserId")
    public List<BusinessEntity> getByUserId(@RequestParam("id") Long userId) {
        return businessServiceImpl.getBusinessesByUserId(userId);
    }

    @PostMapping("/addStaff")
    public Staff addStaff(@RequestBody Staff staff) {
        return userServiceImpl.addStaff(staff);
    }
    
    // UPDATE
    @PutMapping("/updateStaff/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable("id") Long id, @RequestBody Staff staff) {
        return ResponseEntity.ok(userServiceImpl.updateStaff(id, staff));
    }

    // DELETE (mistake entry)
    @DeleteMapping("/deleteStaff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable("id") Long id) {
    	userServiceImpl.deleteStaff(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    // MARK AS LEFT
    @PutMapping("/markStaffAsLeft/{id}")
    public ResponseEntity<?> markStaffAsLeft(@PathVariable("id") Long id,
                                      @RequestParam("reason") String reason) {
    	userServiceImpl.markStaffAsLeft(id, reason);
        return ResponseEntity.ok("Staff marked as left");
    }

    // REJOIN
    @PutMapping("/rejoinStaff/{id}")
    public ResponseEntity<?> rejoinStaff(@PathVariable("id") Long id,
                                   @RequestParam("salary") Long salary) {
    	userServiceImpl.rejoinStaff(id, salary);
        return ResponseEntity.ok("Staff rejoined successfully");
    }

    @PostMapping("/updateSalary")
    public void updateSalary(@RequestParam("id") Long id,
                             @RequestParam("salary") Long salary,
                             @RequestParam("isCorrection") boolean isCorrection) {
        userServiceImpl.updateSalary(id, salary, isCorrection, false);
    }
    
 // ✅ Add Attendance
    @PostMapping("/addAttendance")
    public Attendance addAttendance(
            @RequestParam("staffId") Long staffId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("present") Double present) {

        return userServiceImpl.addAttendance(staffId, date, present);
    }

    // ✅ Update Attendance
    @PutMapping("/updateAttendance/{attendanceId}")
    public Attendance updateAttendance(
            @PathVariable("attendanceId") Long attendanceId,
            @RequestParam("present") Double present) {

        return userServiceImpl.updateAttendance(attendanceId, present);
    }

    // ✅ Get Attendance by Staff & Date
    @GetMapping("/getAttendance")
    public Attendance getAttendance(
            @RequestParam("staffId") Long staffId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return userServiceImpl.getAttendanceByStaffAndDate(staffId, date);
    }

    // ✅ Get All Attendance for a Staff
    @GetMapping("/getAttendanceBystaff/{staffId}")
    public List<AttendanceDTO> getAttendanceByStaff(@PathVariable("staffId") Long staffId) {
        return userServiceImpl.getAttendanceByStaff(staffId);
    }
    
    @GetMapping("/tenant/location")
    public String getTenantLatiLongi(HttpServletRequest request) {
        return userServiceImpl.getTenantLatiLongi(request);
    }
    
}
