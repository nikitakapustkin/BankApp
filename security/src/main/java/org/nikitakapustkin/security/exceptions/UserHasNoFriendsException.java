package org.nikitakapustkin.security.exceptions;

public class UserHasNoFriendsException extends RuntimeException {

  private final String message;

  public UserHasNoFriendsException(String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return this.message;
  }
}
