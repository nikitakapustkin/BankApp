package org.nikitakapustkin.security.exceptions;

import lombok.Getter;
import org.nikitakapustkin.security.enums.ErrorCode;

@Getter
public class BankServiceException extends RuntimeException {
  private final int status;
  private final ErrorCode error;
  private final String remotePath;

  public BankServiceException(int status, ErrorCode error, String message, String remotePath) {
    super(message);
    this.status = status;
    this.error = error;
    this.remotePath = remotePath;
  }
}
