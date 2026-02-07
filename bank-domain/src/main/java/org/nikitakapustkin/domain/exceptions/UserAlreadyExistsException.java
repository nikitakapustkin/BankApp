package org.nikitakapustkin.domain.exceptions;

/**
 * Exception thrown when a user already exists.
 * This exception is used when an attempt is made to create a user that already exists in the system.
 */
public class UserAlreadyExistsException extends RuntimeException {

    private final String message;

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message to be associated with this exception.
     */
    public UserAlreadyExistsException(String message) {
        this.message = message;
    }

    /**
     * Retrieves the detail message for this exception.
     *
     * @return the detail message string.
     */
    @Override
    public String getMessage() {
        return this.message;
    }
}
