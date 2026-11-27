package com.user.jwtauthentication;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GoogleAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public GoogleAuthenticationSuccessHandler(
            JwtUtil jwtUtil,
            ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User googleUser =
                (OAuth2User) authentication.getPrincipal();

        String email = googleUser.getAttribute("email");
        Boolean emailVerified =
                googleUser.getAttribute("email_verified");

        if (email == null || !Boolean.TRUE.equals(emailVerified)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Google email is not verified");
            return;
        }

        // TODO: Find or register this user in your database.
        // Ensure your existing account-linking rules are followed.

        String authToken = jwtUtil.generateToken(email);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), Map.of(
        		"authentication", authToken,
                "status", "Login Successful"
        ));
    }
}