package com.user.jwtauthentication;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {
    private static final SecretKey SECRET_KEY = JwtKeyManager.getSecretKey();
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Token blacklist to track logged-out tokens
    private static final ConcurrentHashMap<String, Boolean> tokenBlacklist = new ConcurrentHashMap<>();

    // Generate JWT token without expiration
    public String generateToken(String username) {
        try {
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())  // Only issuedAt, no expiration
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token", e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    // Validate token: ensure it matches username and is not blacklisted
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            if (tokenBlacklist.containsKey(token)) {
                logger.warn("Token is blacklisted (logged out)");
                return false;
            }

            String username = getUsernameFromToken(token);
            return username != null && username.equals(userDetails.getUsername());
        } catch (Exception e) {
            logger.warn("Invalid JWT token", e);
            return false;
        }
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            logger.error("Error extracting username from token", e);
            return null;
        }
    }

    // Blacklist token on logout
    public void blacklistToken(String token) {
        tokenBlacklist.put(token, true);
    }

    // Method to parse claims from token
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("JWT parsing error: " + e);
            throw new RuntimeException("JWT parsing error: ", e);
        }
    }
}
