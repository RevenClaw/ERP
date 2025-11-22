package edu.univ.erp.auth;

/**
 * Abstraction for hashing and verifying passwords.
 */
public interface PasswordHasher {

    String hash(char[] rawPassword);

    boolean matches(char[] rawPassword, String hashedPassword);
}

