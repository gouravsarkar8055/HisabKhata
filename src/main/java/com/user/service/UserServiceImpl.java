package com.user.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.user.api_response.ApiResponse;
import com.user.dto.AttendanceDTO;
import com.user.entity.Attendance;
import com.user.entity.CustomUserDetails;
import com.user.entity.FinanceEntry;
import com.user.entity.SalaryPayment;
import com.user.entity.Staff;
import com.user.entity.StaffEmploymentHistory;
import com.user.entity.StaffSalary;
import com.user.entity.TempUser;
import com.user.entity.UserEntity;
import com.user.enums.FinanceType;
import com.user.enums.SalaryType;
import com.user.repository.AttendanceRepository;
import com.user.repository.FinanceEntryRepository;
import com.user.repository.SalaryPaymentRepository;
import com.user.repository.StaffEmploymentHistoryRepository;
import com.user.repository.StaffRepository;
import com.user.repository.StaffSalaryRepository;
import com.user.repository.UserEntityRepo;
import com.user.repository.UserRepo;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Service
public class UserServiceImpl implements UserDetailsService{
	
	@Autowired
	private final UserRepo userRepo;
	
	@Autowired
	private SalaryPaymentRepository salaryPaymentRepo;
	
	@Autowired
	private StaffRepository staffRepo;
	
	@Autowired
	private StaffSalaryRepository salaryRepo;
	
	@Autowired
	private FinanceEntryRepository financeRepo;
	
	@Autowired
	private AttendanceRepository attendanceRepo;
	
	@Autowired
	private final UserEntityRepo userEntityRepo;
	
	@Autowired
	private StaffEmploymentHistoryRepository staffEmploymentHistoryRepo;
	
	@Autowired
    private final JavaMailSender mailSender;
	
	@Value("${fromEmail}")
    private String fromEmail;
	
	@Value("${companyName}")
    private String companyName;
	
	@Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    public UserServiceImpl(UserRepo userRepo, UserEntityRepo userEntityRepo, JavaMailSender mailSender) {
		this.userRepo = userRepo;
		this.userEntityRepo = userEntityRepo;
		this.mailSender = mailSender;
        // Initialize Twilio with account SID and auth token
    }
    @PostConstruct  // Add this annotation to initialize Twilio after properties are set
    public void init() {
        Twilio.init(accountSid, authToken);
    }
	private static final int OTP_EXPIRY_DURATION = 2;
	
	
	private static final int codeLength = 4;
	private static int passwordLength = 8;
	private static final Random random = new SecureRandom();
	private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	public String signinLoginWithEmail(String email) {

	    UserEntity user = userEntityRepo.findByEmail(email);
	    boolean isNewUser = false;

	    if (user == null) {
	        user = new UserEntity();
	        user.setEmail(email);
	        isNewUser = true;
	    }

	    String otp = getOtp();
	    LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_DURATION);

	    user.setOtp(otp);
	    user.setExpiryDate(expiryTime);
	    userEntityRepo.save(user);

	    String formattedDateTime = LocalDateTime.now()
	            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

	    sendEmail(email, otp, formattedDateTime);

	    return isNewUser ? "New User Added Successfully" : "OTP sent successfully";
	}
	
	public String verifyEmailOtp(String email, String otp) {
    	UserEntity user = userEntityRepo.findByEmail(email);
    	
        if (user != null && otp.equals(user.getOtp())) {
        	if (LocalDateTime.now().isBefore(user.getExpiryDate())) {
            	//sendVerficationSuccessEmail(email,"User");
                return "verified";
        	}
        	else {
        		return "OTP expired";
        	}
        }
        return "OTP verification failed";
    }
	
	public String addTempUser(TempUser temp) {
		TempUser tempUser = userRepo.findByEmail(temp.getEmail());
		UserEntity user = userEntityRepo.findByEmail(temp.getEmail());
		if(user == null) {
			String hashedPassword = hashPassword(temp.getPassword());
			temp.setPassword(hashedPassword);
			String otp = getOtp();
			LocalDateTime plusMinutes = LocalDateTime.now().plusMinutes(OTP_EXPIRY_DURATION);
			temp.setOtp(otp);
			temp.setExpiryDate(plusMinutes); // Set expiry date
			LocalDateTime currentDateTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        String formattedDateTime = currentDateTime.format(formatter);
	        if(tempUser == null)
	        	userRepo.save(temp);
	        else
	        {
	    		if(tempUser != null && LocalDateTime.now().isAfter(tempUser.getExpiryDate())) {
	    			tempUser.setUserName(temp.getUserName());
	    			tempUser.setPassword(temp.getPassword());
	    			tempUser.setPhone(temp.getPhone());
	    			tempUser.setAddress(temp.getAddress());
	    			tempUser.setShopName(temp.getShopName());
	    			tempUser.setOtp(otp);
	    			tempUser.setExpiryDate(plusMinutes);
	    			userRepo.save(tempUser);
	    			sendEmail(temp.getEmail(),otp, formattedDateTime);
	    			return "TempUser Updated Successfully";
	    		}else
	    			return "You can add TempUser after otp expire";
	        }
			sendEmail(temp.getEmail(),otp, formattedDateTime);
			return "TempUser added Successfully";
			}
			return "User Already available";
	}
	
	public static String getOtp() {
		String sixDigitCode = "0123456789";
		StringBuilder pw = new StringBuilder();
		for (int i = 0; i < codeLength; i++) {
			int index = random.nextInt(sixDigitCode.length());
			pw.append(sixDigitCode.substring(index, index + 1));
		}
		return pw.toString();	
		}
	
	public static String generateRandomPassword() {
		String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";
		StringBuilder pw = new StringBuilder();
		for (int i = 0; i < passwordLength; i++) {
			int index = random.nextInt(letters.length());
			pw.append(letters.substring(index, index + 1));
		}
		return pw.toString();
	}
	
	
	public void sendEmail(String toEmail, String otp, String formattedDateTime) {

	    String subject = otp + " is your OTP to verify your email";

	    String htmlContent =
	            "<!DOCTYPE html>" +
	            "<html>" +
	            "<head>" +
	            "<meta charset='UTF-8'>" +
	            "</head>" +
	            "<body style='font-family:Arial,Helvetica,sans-serif; background-color:#f6f6f6; padding:20px;'>" +

	            "<div style='max-width:600px; margin:auto; background:#ffffff; padding:20px; border-radius:8px;'>" +

	            "<p>Dear User,</p>" +

	            "<p>Your <b>One Time Password (OTP)</b> is:</p>" +

	            "<div style='text-align:center; margin:20px 0;'>" +
	            "<span style='font-size:28px; color:#2e86de; font-weight:bold; letter-spacing:3px;'>"
	            + otp +
	            "</span>" +
	            "</div>" +

	            "<p>This OTP is valid for <b>2 minutes</b>.</p>" +

	            "<p style='font-size:12px; color:#555;'>Generated at: "
	            + formattedDateTime +
	            "</p>" +

	            "<hr style='margin:20px 0;'/>" +

	            "<p style='font-size:12px; color:gray;'>" +
	            "If you did not request this OTP, please ignore this email.<br/>" +
	            "This is an auto-generated email. Please do not reply." +
	            "</p>" +

	            "</div>" +
	            "</body>" +
	            "</html>";

	    try {
	        MimeMessage message = mailSender.createMimeMessage();

	        MimeMessageHelper helper =
	                new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

	        // ✅ VERIFIED Brevo sender
	        helper.setFrom(new InternetAddress("gouravinspira01@gmail.com", "HisabKhata"));

	        helper.setTo(toEmail);
	        helper.setSubject(subject);
	        helper.setText(htmlContent, true); // true = HTML

	        mailSender.send(message);

	    } catch (Exception e) {
	        // 🔴 Log full error (important for SMTP issues)
	        throw new RuntimeException("Failed to send OTP email to " + toEmail, e);
	    }
	}
    
    public String verifyOtp(String email, String otp) {
    	TempUser tempUser = userRepo.findByEmail(email);
    	
        if (tempUser != null && otp.equals(tempUser.getOtp())) {
        	if (LocalDateTime.now().isBefore(tempUser.getExpiryDate())) {
        		UserEntity userEntity = new UserEntity();
        		userEntity.setUserName(tempUser.getUserName());
            	userEntity.setPassword(tempUser.getPassword());
            	userEntity.setEmail(tempUser.getEmail());
            	userEntity.setAddress(tempUser.getAddress());
            	userEntity.setShopName(tempUser.getShopName());
            	userEntity.setPhone(tempUser.getPhone());
            	userEntity.setStatus("InActive");
            	userEntity.setAccountCreated(LocalDateTime.now());
            	userRepo.save(userEntity);
            	userRepo.delete(tempUser);
            	//sendVerficationSuccessEmail(email,tempUser.getUserName());
                return "verified";
        	}
        	else {
        		return "OTP expired";
        	}
        }
        return "OTP verification failed";
    }
    
    public String resendOtp(String email) {
    	TempUser tempUser = userRepo.findByEmail(email);
        if (tempUser != null) {
            if (LocalDateTime.now().isAfter(tempUser.getExpiryDate())) {
            	tempUser.setOtp(getOtp());
            	tempUser.setExpiryDate(LocalDateTime.now().plusMinutes(OTP_EXPIRY_DURATION));
            	LocalDateTime currentDateTime = LocalDateTime.now();
        		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = currentDateTime.format(formatter);
        		userRepo.save(tempUser);
        		sendEmail(tempUser.getEmail(),tempUser.getOtp(), formattedDateTime);
               return "otp resend successfully";
            } else {
                throw new RuntimeException("OTP has not expired yet. Please wait before resending.");
            }
        } else {
           return "email is incorrect or empty";
        }
    }
    
    public void sendSms(String toPhoneNumber, String messageBody) {
        Message message = Message.creator(
                new PhoneNumber("+91"+toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                messageBody)
                .create();
        System.out.println("SMS sent: " + message.getSid());
    }
    
    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    public String login(String email, String rawPassword) {
    	UserEntity user = userEntityRepo.findByEmail(email);
    	if((email == null || email.isEmpty()) && (rawPassword == null || rawPassword.isEmpty()))
			return "Please Enter Email and Password";
		else if(email.isEmpty())
			return "Please Enter Email";
		
		else if (user != null) {
            // Compare raw password with hashed password
            if(!rawPassword.isEmpty()) {
            	boolean matches = matches(rawPassword, user.getPassword());
                if(matches)
                	return "Login Successfull";
                else
                	return "Incorrect Password";
            }else {
            	return "Please enter Password";
            }
        }
        
        return "Email not found. Please signUp first"; // User not found
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userEntityRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Return CustomUserDetails with userId
        return new CustomUserDetails(
                user.getId(),          // ✅ Include userId
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()      // Empty list for authorities
        );
    }

    
    public void sendVerficationEmail(String email, String userName){
    	String subject = "Account Verification - Action Required";
        String text = "Dear "+userName+","

		+"Thank you for registering with [Your Company Name]. We're excited to have you on board!"
		
		+"We wanted to inform you that we are processing your account verification request. "
		+ "Your account will be fully verified and ready for use within the next 24 hours."
		
		+"What to Expect:"
		+"Verification Completion: Your account will be verified and activated within 24 hours."
		+"Notification: You will receive a confirmation email once the verification process is completed.";
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
        	MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(fromEmail, companyName);
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(text);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    
    public void sendVerficationSuccessEmail(String email, String userName){
    	String subject = "Account Verification Successful";
        String text = "Dear "+userName+","

		+"We are pleased to inform you that your account with [Your Company Name] has been successfully"
		
		+"verified! "
		+ "Your account is now fully activated and ready for use. You can log in anytime to start enjoying all the"
		+"features and services we offer."
		
		+"What’s Next?"
		+"Login: You can now log in to your account and begin using [Your Company Name]."
		+"Access Services: Take full advantage of all available services and features.";
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
        	MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(fromEmail, companyName);
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(text);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    
    public void sendVerficationFailEmail(String email, String userName){
    	String subject = "Account Verification Failed - Please Register Again";
        String text = "Dear "+userName+","

		+"We regret to inform you that the verification of your account with [Your Company Name] has not"
		
		+"been successful."
		
		+"Reason for Failure:"
		+"Your Shop is not present on that area."
		+"What You Can Do Next:"
		+"Register Again: We recommend you re-register with the correct information to initiate a new"
		+"verification process."
		+"Review Information: Please ensure all details are accurate before submitting again.";
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
        	MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(fromEmail, companyName);
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(text);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    
    public ApiResponse<UserEntity> getUserData() throws UsernameNotFoundException{
    	String email = getCurrentUserDetails().getUsername();
    	UserEntity user = userEntityRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return ApiResponse.<UserEntity>builder()
	            .success(true)
	            .message("Details fetch successfully")
	            .data(user)
	            .timestamp(LocalDateTime.now())
	            .build();
    }
    
    public ApiResponse<UserEntity> updateUserData(Long userId, UserEntity updatedUser) {

        Optional<UserEntity> optionalUser = userEntityRepo.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        UserEntity existingUser = optionalUser.get();

        // Update fields (only editable fields)
        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());

        // Do NOT update email if it's read-only
        // existingUser.setEmail(updatedUser.getEmail());
        userEntityRepo.save(existingUser);

        return ApiResponse.<UserEntity>builder()
	            .success(true)
	            .message("User updated successfully")
	            .data(existingUser)
	            .timestamp(LocalDateTime.now())
	            .build();
    }
    
    public UserDetails getCurrentUserDetails() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return (UserDetails) authentication.getPrincipal();
    }
    
    @Transactional
    public Staff addStaff(Staff staff) {

        // Set default flags
        staff.setIsActive(true);
        staff.setIsDeleted(false);
        
        // ❌ removed: staff.setJoinDate(...)

        Staff savedStaff = staffRepo.save(staff);

        // ✅ Create employment history (JOIN entry)
        StaffEmploymentHistory history = new StaffEmploymentHistory();
        history.setStaff(savedStaff);
        history.setJoinDate(LocalDate.now());
        history.setIsActive(true); // current active record

        staffEmploymentHistoryRepo.save(history);

        // ✅ Create initial salary
        StaffSalary salary = new StaffSalary();
        salary.setStaff(savedStaff);
        salary.setSalary(staff.getCurrentSalary());
        salary.setSalaryType(staff.getSalaryType());
        salary.setEffectiveFrom(LocalDate.now());

        salaryRepo.save(salary);

        return savedStaff;
    }
    
    @Transactional
    public Staff updateStaff(Long staffId, Staff updated) {

        Staff existing = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (Boolean.TRUE.equals(existing.getIsDeleted())) {
            throw new RuntimeException("Staff is deleted");
        }

        existing.setName(updated.getName());
        existing.setRole(updated.getRole());

        if (updated.getSalaryType() != null &&
            updated.getSalaryType() != existing.getSalaryType()) {

            existing.setSalaryType(updated.getSalaryType());

            StaffSalary current = salaryRepo.findCurrentSalary(staffId);
            if (current != null) {
                current.setSalaryType(updated.getSalaryType());
                salaryRepo.save(current);
            }
        }

        return staffRepo.save(existing);
    }

    @Transactional
    public void deleteStaff(Long staffId) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

      //  boolean hasSalary = salaryRepo.existsByStaffId(staffId);
        boolean hasAttendance = attendanceRepo.existsByStaffId(staffId);
        boolean hasPayments = salaryPaymentRepo.existsByStaffId(staffId);

        if (hasAttendance || hasPayments) {
            throw new RuntimeException("Cannot delete. Use deactivate.");
        }

        // Soft delete
        staff.setIsDeleted(true);
        staff.setIsActive(false);

        staffRepo.save(staff);
    }

    @Transactional
    public void markStaffAsLeft(Long staffId, String reason) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (!staff.getIsActive()) {
            throw new RuntimeException("Already inactive");
        }

        LocalDate today = LocalDate.now();

        // ✅ Update staff status
        staff.setIsActive(false);
        staffRepo.save(staff);

        // ✅ Get current active employment record
        StaffEmploymentHistory history =
                staffEmploymentHistoryRepo.findActiveByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Active employment record not found"));

        // ✅ Update exit details in history
        history.setExitDate(today);
        history.setExitReason(reason);
        history.setIsActive(false);

        staffEmploymentHistoryRepo.save(history);

        // ✅ Close current salary
        StaffSalary current = salaryRepo.findCurrentSalary(staffId);
        if (current != null) {
            current.setEffectiveTo(today.minusDays(1));
            salaryRepo.save(current);
        }
    }

    @Transactional
    public void rejoinStaff(Long staffId, Long newSalary) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (staff.getIsActive()) {
            throw new RuntimeException("Already active");
        }

        LocalDate today = LocalDate.now();

        // ✅ Update staff status
        staff.setIsActive(true);
        staff.setCurrentSalary(newSalary);
        staffRepo.save(staff);

        // ✅ Create NEW employment history (rejoin entry)
        StaffEmploymentHistory history = new StaffEmploymentHistory();
        history.setStaff(staff);
        history.setJoinDate(today);
        history.setIsActive(true);

        staffEmploymentHistoryRepo.save(history);

        // ✅ Add new salary (do NOT reuse old one)
        updateSalary(staffId, newSalary, false, true);
    }
    
    @Transactional
    public void updateSalary(Long staffId, Long newSalary, boolean isCorrection, boolean isRejoin) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (Boolean.TRUE.equals(staff.getIsDeleted())) {
            throw new RuntimeException("Staff deleted");
        }

        StaffSalary current = salaryRepo.findCurrentSalary(staffId);

        if (current == null && !isRejoin) {
            throw new RuntimeException("No salary found");
        }

        LocalDate today = LocalDate.now();

        if (isCorrection) {

            Long count = salaryPaymentRepo.countSalaryPaid(
                    staffId,
                    current.getEffectiveFrom(),
                    today
            );

            if (count != null && count > 0) {
                throw new RuntimeException("Salary already processed");
            }

            current.setSalary(newSalary);
            salaryRepo.save(current);

        } else {

        	if(!isRejoin) {
        		current.setEffectiveTo(today.minusDays(1));
        		salaryRepo.save(current);
        	}

            StaffSalary newSal = new StaffSalary();
            newSal.setStaff(staff);
            newSal.setSalary(newSalary);
            newSal.setSalaryType(staff.getSalaryType());
            newSal.setEffectiveFrom(today);

            salaryRepo.save(newSal);
        }

        staff.setCurrentSalary(newSalary);
        staffRepo.save(staff);
    }
    
    public Long calculateSalary(Long staffId, LocalDate start, LocalDate end) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (!staff.getIsActive()) {
            throw new RuntimeException("Staff is inactive");
        }

        List<Attendance> attendanceList =
            attendanceRepo.findByStaffIdAndDateBetween(staffId, start, end);

        double totalDays = attendanceList.stream()
                .mapToDouble(Attendance::getPresent)
                .sum();

        StaffSalary salary = salaryRepo.findActiveSalary(staffId, start);

        if (salary == null) return 0L;

        switch (salary.getSalaryType()) {

            case DAILY:
                return (long) (totalDays * salary.getSalary());

            case MONTHLY:
                long perDay = salary.getSalary() / 30;
                return (long) (perDay * totalDays);

            case WEEKLY:
                return salary.getSalary();

            default:
                return 0L;
        }
    }
    
    @Transactional
    public void paySalary(Long staffId, LocalDate start, LocalDate end) {

        Long amount = calculateSalary(staffId, start, end);

        if (amount == null || amount <= 0) {
            throw new RuntimeException("Invalid salary amount");
        }

        Staff staff = staffRepo.findById(staffId).orElseThrow();

        SalaryPayment payment = new SalaryPayment();
        payment.setStaff(staff);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDate.now());
        payment.setPeriod(start.getMonth() + "-" + start.getYear());

        salaryPaymentRepo.save(payment);

        FinanceEntry entry = new FinanceEntry();
        entry.setAmount(amount);
        entry.setType(FinanceType.EXPENSE);
        entry.setCategory("SALARY");
        entry.setDate(LocalDate.now());

        financeRepo.save(entry);
    }
    
 // ✅ ADD Attendance
    @Transactional
    public Attendance addAttendance(Long staffId, LocalDate date, Double present) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Optional: prevent duplicate entry for same date
        Optional<Attendance> existing = attendanceRepo.findByStaffAndDate(staffId, date);
        if (existing.isPresent()) {
            throw new RuntimeException("Attendance already marked for this date");
        }

        Attendance attendance = new Attendance();
        attendance.setStaff(staff);
        attendance.setDate(date);
        attendance.setPresent(present);

        return attendanceRepo.save(attendance);
    }

    // ✅ UPDATE Attendance
    @Transactional
    public Attendance updateAttendance(Long attendanceId, Double present) {

        Attendance attendance = attendanceRepo.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        attendance.setPresent(present);

        return attendanceRepo.save(attendance);
    }
    
    public Attendance getAttendanceByStaffAndDate(Long staffId, LocalDate date) {

        return attendanceRepo.findByStaffAndDate(staffId, date)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
    }

    public List<AttendanceDTO> getAttendanceByStaff(Long staffId) {

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        return attendanceRepo.findByStaffId(staff.getId())
                .stream()
                .map(a -> new AttendanceDTO(
                        a.getId(),
                        a.getDate(),
                        a.getPresent()
                ))
                .toList();
    }
    
    public String getTenantLatiLongi(HttpServletRequest request) {
    	String latitude = request.getParameter("tenantLatitude");
		String longitude = request.getParameter("tenantLangitude");
    	String address1 = request.getParameter("tenantAddress");
    	String address2 = "";
		String city = request.getParameter("tenantCity");
		String zipCode = request.getParameter("tenantZipcode");
		String country = request.getParameter("tenantCountry");
		String state = request.getParameter("tenantState");
    	String tenantAddress = address1 + "," + address2 + "," + city + ","
				+ state + ", " + country + ", " + zipCode;
		String jsonResponse = null;
		try {
			jsonResponse = getText("https://maps.google.com/maps/api/geocode/json?address="
					+ URLEncoder.encode(tenantAddress, "UTF-8")
					+ "&sensor=false&key=AIzaSyAXEHjz5uTArWEC0q8-zpu_xXuHXJ2zOl8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jresp = getJSONData(jsonResponse);
		if (jresp.has("lat") && jresp.has("lng")) {
			latitude = String.valueOf(jresp.getDouble("lat"));
			longitude = String.valueOf(jresp.getDouble("lng"));
		} else {
			String tenantAddress2 = state + ", " + country;
			String jsonResponse2 = null;
			try {
				jsonResponse2 = getText("https://maps.google.com/maps/api/geocode/json?address="
						+ URLEncoder.encode(tenantAddress2, "UTF-8")
						+ "&sensor=false&key=AIzaSyAXEHjz5uTArWEC0q8-zpu_xXuHXJ2zOl8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONObject jresp2 = getJSONData(jsonResponse2);
			if (jresp2.has("lat") && jresp2.has("lng")) {
				latitude = String.valueOf(jresp2.getDouble("lat"));
				longitude = String.valueOf(jresp2.getDouble("lng"));
			}
		}
    	return "latitude: "+latitude+" & longitude: "+longitude;
    }
    
    public String getText(String url) {

		StringBuilder response = new StringBuilder();
		try {
			URL website = new URL(url);
			URLConnection connection = website.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				response.append(inputLine);
			in.close();
		} catch (Exception e) {
			String errorMessage = "Error in read JSON Response : ";
			logger.error(errorMessage + e.getMessage(), e);
		}
		return response.toString();
	}
    
    public JSONObject getJSONData(String jsonText) {
		JSONObject latlang = null;
		try {

			JSONObject resp = new JSONObject(jsonText);
			JSONArray jarry = resp.getJSONArray("results");

			if (jarry.length() > 0)
				latlang = jarry.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
			else
				latlang = new JSONObject();

		} catch (Exception e) {
			latlang = new JSONObject();
			logger.error("Error in get the JSON response in getJSONData(): " + e.getMessage(), e);
		}
		return latlang;
	}
    
}
