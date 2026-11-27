package com.user.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.user.dto.GoogleLoginRequest;
import com.user.jwtauthentication.JwtUtil;
import com.user.service.GoogleAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

	@Autowired
    private final GoogleAuthService googleAuthService;
    
    @Autowired
    private final JwtUtil jwtUtil;
    
    @Autowired
	private UserDetailsService userDetailsService;
    
    private static final Logger logger =
            LoggerFactory.getLogger(GoogleAuthController.class);

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(
            @RequestBody GoogleLoginRequest request) {

        try {
            GoogleIdToken.Payload payload =
                googleAuthService.verifyToken(
                    request.getIdToken()
                );

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            Boolean emailVerified = payload.getEmailVerified();

            if (!Boolean.TRUE.equals(emailVerified)) {
                return ResponseEntity.badRequest()
                    .body("Google email is not verified");
            }

            // Find existing user or create a new user
            // Generate your application JWT
            // Return user details and JWT
            
    	    try {

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
    	                "status", "Google authentication successful"
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

        } catch (Exception e) {
            logger.error("Google authentication failed", e);

            return ResponseEntity.badRequest()
                    .body("Google authentication failed");
        }
    }
}