package edu.univ.erp.api.types;

import java.util.Arrays;
import java.util.Objects;

/**
 * DTO representing login input from the UI.
 */
public final class LoginRequest {

    private final String username;
    private final char[] password;

    public LoginRequest(String username, char[] password) {
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
    }

    public String username() {
        return username;
    }

    public char[] password() {
        return password;
    }

    public void clearPassword() {
        Arrays.fill(password, '\0');
    }
}

