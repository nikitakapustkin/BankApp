package org.nikitakapustkin.domain.exceptions;

/**
 * Exception thrown when a user is not found. This exception is used when an operation is attempted
 * on a user that does not exist in the system.
 */
public class UserNotFoundException extends RuntimeException {

  private final String message;

  /**
   * Constructs a new UserNotFoundException with the specified detail message.
   *
   * @param message the detail message to be associated with this exception.
   */
  public UserNotFoundException(String message) {
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
