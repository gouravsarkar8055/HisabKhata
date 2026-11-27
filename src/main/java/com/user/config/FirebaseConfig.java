package com.user.config;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    @PostConstruct
    public void initializeFirebase() throws IOException {

        if (FirebaseApp.getApps().isEmpty()) {

            try (FileInputStream serviceAccount =
                    new FileInputStream(credentialsPath)) {

                FirebaseOptions options =
                    FirebaseOptions.builder()
                        .setCredentials(
                            GoogleCredentials.fromStream(serviceAccount)
                        )
                        .build();

                FirebaseApp.initializeApp(options);
            }
        }
    }
}
