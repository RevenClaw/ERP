package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility to generate and verify BCrypt hash for password123.
 * Run this to get a verified hash for the seed data.
 */
public final class VerifyAndGenerateHash {

    public static void main(String[] args) {
        String password = "password123";
        
        // Generate a new hash
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("Generated Hash: " + hash);
        System.out.println("========================================");
        
        // Verify it works
        boolean matches = BCrypt.checkpw(password, hash);
        System.out.println("Verification: " + (matches ? "✓ SUCCESS" : "✗ FAILED"));
        System.out.println("========================================");
        System.out.println();
        System.out.println("Use this hash in auth_seed.sql:");
        System.out.println(hash);
    }
}

