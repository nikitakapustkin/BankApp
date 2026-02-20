package org.nikitakapustkin.domain.exceptions;

/**
 * Exception thrown when a user has no friends. This exception is used when an operation is
 * attempted that requires a user to have friends but they do not have any.
 */
public class UserHasNoFriendsException extends RuntimeException {

  private final String message;

  /**
   * Constructs a new UserHasNoFriendsException with the specified detail message.
   *
   * @param message the detail message to be associated with this exception.
   */
  public UserHasNoFriendsException(String message) {
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
