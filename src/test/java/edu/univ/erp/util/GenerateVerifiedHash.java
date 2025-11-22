package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Generate a verified BCrypt hash for password123.
 */
public final class GenerateVerifiedHash {

    public static void main(String[] args) {
        String password = "password123";
        
        // Generate hash
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        
        // Verify it
        boolean verified = BCrypt.checkpw(password, hash);
        
        System.out.println("Password: password123");
        System.out.println("Hash: " + hash);
        System.out.println("Verified: " + verified);
        System.out.println();
        System.out.println("Copy this hash to auth_seed.sql:");
        System.out.println(hash);
    }
}

