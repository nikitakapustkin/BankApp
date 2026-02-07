package org.nikitakapustkin.security.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.nikitakapustkin.security.enums.ErrorCode;

import java.time.Instant;

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
