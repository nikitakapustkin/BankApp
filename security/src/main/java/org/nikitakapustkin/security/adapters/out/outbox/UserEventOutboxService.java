package org.nikitakapustkin.security.adapters.out.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.UserCreatedPayload;
import org.nikitakapustkin.security.application.ports.out.UserEventPublisherPort;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserEventOutboxService implements UserEventPublisherPort {
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.user}")
    private String userTopic;

    @Value("${kafka.producer.name:security-service}")
    private String producer;

    @Override
    public void enqueueUserCreated(UserCreateRequestDto request, UUID userId) {
        String description = "User created: " + request.getLogin();
        UserCreatedPayload payload = new UserCreatedPayload(
                userId,
                request.getLogin(),
                request.getName(),
                request.getAge(),
                toContractSex(request.getSex()),
                toContractHairColor(request.getHairColor()),
                description
        );

        EventEnvelope<UserCreatedPayload> envelope = new EventEnvelope<>(
                userId,
                EventTypes.USER_CREATED,
                Instant.now(),
                userId,
                producer,
                payload
        );

        try {
            String json = objectMapper.writeValueAsString(envelope);
            String key = userId != null ? userId.toString() : null;
            outboxRepository.save(OutboxEventEntity.newEvent(userTopic, key, envelope.eventType(), json));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize user created event", e);
        }
    }

    private static Sex toContractSex(String sex) {
        if (sex == null || sex.isBlank()) {
            return null;
        }
        return Sex.valueOf(sex.toUpperCase(Locale.ROOT));
    }

    private static HairColor toContractHairColor(String hairColor) {
        if (hairColor == null || hairColor.isBlank()) {
            return null;
        }
        return HairColor.valueOf(hairColor.toUpperCase(Locale.ROOT));
    }
}
