package org.nikitakapustkin.security.exceptions;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.nikitakapustkin.security.enums.ErrorCode;

@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {
  private Instant timestamp;
  private int status;
  private ErrorCode error;
  private String message;
  private String path;
}
