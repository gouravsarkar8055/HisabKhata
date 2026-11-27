
package com.user.service;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class GoogleAuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(GoogleAuthService.class);

    @Value("${google.client.id}")
    private String clientId;

    public GoogleIdToken.Payload verifyToken(String idToken)
            throws Exception {

        if (idToken == null || idToken.trim().isEmpty()) {
            throw new RuntimeException("Google ID Token is empty");
        }

        // Diagnostic only: these claims are not yet trusted.
        GoogleIdToken parsedToken = GoogleIdToken.parse(
                GsonFactory.getDefaultInstance(), idToken);

        GoogleIdToken.Payload parsedPayload =
                parsedToken.getPayload();

        logger.info("Token Audience: {}", parsedPayload.getAudience());
        logger.info("Token Issuer: {}", parsedPayload.getIssuer());
        logger.info("Token Expiration: {}",
                parsedPayload.getExpirationTimeSeconds());
        logger.info("Configured Client ID: {}", clientId);

        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(clientId))
                        .build();

        GoogleIdToken googleIdToken = verifier.verify(idToken);

        if (googleIdToken == null) {
            throw new RuntimeException("Invalid Google ID Token");
        }

        logger.info("Google ID Token verified successfully");

        return googleIdToken.getPayload();
    }
}