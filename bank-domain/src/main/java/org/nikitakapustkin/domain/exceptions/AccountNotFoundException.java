package org.nikitakapustkin.domain.exceptions;

/**
 * Exception thrown when an account is not found. Thrown when account cant be located in repository.
 * RuntimeException
 */
public class AccountNotFoundException extends RuntimeException {
  private final String message;

  /**
   * constructs object with message
   *
   * @param message message with details about exception
   */
  public AccountNotFoundException(String message) {
    this.message = message;
  }

  /**
   * gets message for exception
   *
   * @return message about exception
   */
  @Override
  public String getMessage() {
    return this.message;
  }
}
