package org.nikitakapustkin.bank.contracts.errors;

import java.time.Instant;

public record ApiError(
    Instant timestamp, int status, ErrorCode error, String message, String path) {}
