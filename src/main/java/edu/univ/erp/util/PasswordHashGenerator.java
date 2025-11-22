package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility to generate BCrypt password hashes for seed data.
 * Run this to generate hashes for the auth_seed.sql file.
 */
public final class PasswordHashGenerator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: PasswordHashGenerator <password1> [password2] ...");
            System.out.println("Example: PasswordHashGenerator password123");
            return;
        }

        System.out.println("Generated BCrypt hashes:");
        System.out.println("------------------------");
        for (String password : args) {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
    }
}

