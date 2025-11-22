package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Generate BCrypt password hashes for seed data.
 * Run this test to generate hashes.
 */
public final class GeneratePasswordHashes {

    public static void main(String[] args) {
        String password = "password123";
        
        System.out.println("Generated BCrypt hashes for password: " + password);
        System.out.println("==========================================");
        
        // Generate 4 hashes (one for each test user)
        for (int i = 1; i <= 4; i++) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            System.out.println("Hash " + i + ": " + hash);
        }
    }
}

