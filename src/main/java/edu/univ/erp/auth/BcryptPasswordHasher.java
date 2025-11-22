package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt-based implementation of {@link PasswordHasher}.
 */
public final class BcryptPasswordHasher implements PasswordHasher {

    private static final int ROUNDS = 10;

    @Override
    public String hash(char[] rawPassword) {
        String password = new String(rawPassword);
        return BCrypt.hashpw(password, BCrypt.gensalt(ROUNDS));
    }

    @Override
    public boolean matches(char[] rawPassword, String hashedPassword) {
        try {
            String password = new String(rawPassword);
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception ex) {
            return false;
        }
    }
}

