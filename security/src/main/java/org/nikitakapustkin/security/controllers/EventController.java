package org.nikitakapustkin.security.controllers;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.EventService;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.nikitakapustkin.security.dto.StorageEventResponseDto;
import org.springframework.http.ResponseEntity;
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
public class EventController {
    private final EventService eventService;

    @GetMapping(SecurityApiPaths.EVENTS)
    public ResponseEntity<List<StorageEventResponseDto>> getEvents(
            @RequestParam(required = false, name = "source")
            @Pattern(regexp = "(?i)ALL|USER|ACCOUNT|TRANSACTION", message = "source must be ALL, USER, ACCOUNT or TRANSACTION")
            String source,
            @RequestParam(required = false, name = "eventType")
            String eventType,
            @RequestParam(required = false, name = "entityId")
            UUID entityId,
            @RequestParam(required = false, name = "correlationId")
            UUID correlationId,
            @RequestParam(required = false, name = "transactionType")
            @Pattern(regexp = "(?i)DEPOSIT|WITHDRAWAL|TRANSFER", message = "transactionType must be DEPOSIT, WITHDRAWAL or TRANSFER")
            String transactionType,
            @RequestParam(required = false, name = "limit", defaultValue = "100")
            @Min(value = 1, message = "limit must be >= 1")
            @Max(value = 500, message = "limit must be <= 500")
            Integer limit
    ) {
        String normalizedSource = source == null ? null : source.toUpperCase(Locale.ROOT);
        String normalizedType = transactionType == null ? null : transactionType.toUpperCase(Locale.ROOT);
        return ResponseEntity.ok(eventService.getEvents(
                normalizedSource,
                eventType,
                entityId,
                correlationId,
                normalizedType,
                limit
        ));
    }
}
