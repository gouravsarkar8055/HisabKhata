package com.user.jwtauthentication;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.io.*;
import java.util.Base64;

public class JwtKeyManager {
    private static final String KEY_FILE_PATH = System.getProperty("java.io.tmpdir") + "/jwt_secret.key";
    private static SecretKey SECRET_KEY = null;

    static {
        SECRET_KEY = loadOrGenerateKey();
    }

    public static SecretKey getSecretKey() {
        SECRET_KEY = loadOrGenerateKey(); // Always reload the key
        return SECRET_KEY;
    }

    // ✅ Generate a new key
    private static SecretKey generateNewKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    // ✅ Save the key to a file
    private static void saveKey(SecretKey key) {
        try (FileOutputStream fos = new FileOutputStream(KEY_FILE_PATH)) {
            fos.write(Base64.getEncoder().encode(key.getEncoded()));
        } catch (IOException e) {
            throw new RuntimeException("❌ Error saving secret key", e);
        }
    }

    // ✅ Load the key from the file
    private static SecretKey loadKey() {
        File file = new File(KEY_FILE_PATH);
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] encodedKey = fis.readAllBytes();
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (IOException e) {
            throw new RuntimeException("❌ Error loading secret key", e);
        }
    }

    // ✅ Load or generate key safely
    private static SecretKey loadOrGenerateKey() {
        SecretKey key = loadKey();
        if (key == null) {
            key = generateNewKey();
            saveKey(key);
        }
        return key;
    }

    // ✅ Rotate the secret key (invalidate all tokens)
    public static void rotateSecretKey() {
        SECRET_KEY = generateNewKey(); // Generate a new key
        saveKey(SECRET_KEY); // Save the new key
        System.out.println("🔄 Secret key rotated! New key: " + Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded()));
    }
}
