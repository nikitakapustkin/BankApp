package org.nikitakapustkin.domain.exceptions;

/**
 * Exception thrown when there is not enough money for a transaction. This exception is used when a
 * user attempts a transaction that exceeds their available balance.
 */
public class NotEnoughMoneyException extends RuntimeException {

  private final String message;

  /**
   * Constructs a new NotEnoughMoneyException with the specified detail message.
   *
   * @param message the detail message to be associated with this exception.
   */
  public NotEnoughMoneyException(String message) {
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
