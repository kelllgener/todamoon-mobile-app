package com.toda.todamoon_v1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashPassword {
    // Method to hash the password
    public String hashPassword(String password) {
        try {
            // Create MessageDigest instance for hashing
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Add password bytes to digest
            md.update(password.getBytes());
            // Get the hashed bytes
            byte[] hashedBytes = md.digest();
            // Convert byte array to Base64 for storage (you can choose a different encoding if needed)
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Handle error gracefully
        }
    }
}
