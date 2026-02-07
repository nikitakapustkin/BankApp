package org.nikitakapustkin.security.exceptions;


public class AccountNotFoundException extends RuntimeException {
    private final String message;

    public AccountNotFoundException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
