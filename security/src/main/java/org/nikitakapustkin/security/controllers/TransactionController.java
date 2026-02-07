package org.nikitakapustkin.security.controllers;

import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.AccountService;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.nikitakapustkin.security.dto.TransactionResponseDto;
import org.nikitakapustkin.security.models.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class TransactionController {
    private final AccountService accountService;

    @GetMapping(SecurityApiPaths.USERS_ME_TRANSACTIONS)
    public ResponseEntity<List<TransactionResponseDto>> getUserTransactions(
            @RequestParam(required = false, name = "type")
            @Pattern(regexp = "(?i)DEPOSIT|WITHDRAWAL|TRANSFER", message = "Type must be DEPOSIT, WITHDRAWAL or TRANSFER")
            String type,
            @RequestParam(required = false, name = "accountId") UUID accountId,
            Authentication authentication
    ) {
        UUID userId = requireUserId(authentication);
        String normalizedType = normalizeType(type);
        return ResponseEntity.ok(accountService.getUserTransactions(userId, normalizedType, accountId));
    }

    @GetMapping(SecurityApiPaths.TRANSACTIONS)
    public ResponseEntity<List<TransactionResponseDto>> getTransactions(
            @RequestParam(required = false, name = "type")
            @Pattern(regexp = "(?i)DEPOSIT|WITHDRAWAL|TRANSFER", message = "Type must be DEPOSIT, WITHDRAWAL or TRANSFER")
            String type,
            @RequestParam(required = false, name = "accountId") UUID accountId
    ) {
        String normalizedType = normalizeType(type);
        return ResponseEntity.ok(accountService.getTransactions(normalizedType, accountId));
    }

    private static UUID requireUserId(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("Access denied");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal && jwtPrincipal.userId() != null) {
            return jwtPrincipal.userId();
        }
        throw new AccessDeniedException("Access denied");
    }

    private static String normalizeType(String type) {
        return type == null ? null : type.toUpperCase(Locale.ROOT);
    }
}
