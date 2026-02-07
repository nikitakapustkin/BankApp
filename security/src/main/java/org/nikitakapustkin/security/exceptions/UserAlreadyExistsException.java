package org.nikitakapustkin.security.exceptions;


public class UserAlreadyExistsException extends RuntimeException {

    private final String message;


    public UserAlreadyExistsException(String message) {
        this.message = message;
    }


    @Override
    public String getMessage() {
        return this.message;
    }
}
