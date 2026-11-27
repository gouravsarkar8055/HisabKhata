package com.user.jwtauthentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.user.entity.CustomUserDetails;
import com.user.service.UserServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserServiceImpl userServiceImpl;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserServiceImpl userServiceImpl) {
        this.jwtUtil = jwtUtil;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                // Extract email (username) from the token
                email = jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                logger.error("Failed to extract username from token", e);
            }
        }

        // If the token is valid and no authentication is present
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Load user details based on email (username)
                UserDetails userDetails = userServiceImpl.loadUserByUsername(email);
                
                // If the token is valid, set the authentication in the security context
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("Invalid JWT token or user not found", e);
            }
        }

        // Proceed with the filter chain
        chain.doFilter(request, response);
    }

    /**
     * Retrieves the logged-in user's ID from the SecurityContext.
     */
    public Long getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        return null; // User not authenticated
    }
}
