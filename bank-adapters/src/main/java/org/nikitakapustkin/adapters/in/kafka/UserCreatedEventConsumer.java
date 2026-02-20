package org.nikitakapustkin.adapters.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.nikitakapustkin.application.ports.in.ImportUserUseCase;
import org.nikitakapustkin.application.ports.in.commands.ImportUserCommand;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.UserCreatedPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreatedEventConsumer {
  private final ObjectMapper objectMapper;
  private final ImportUserUseCase importUserUseCase;

  @Value("${kafka.user-events.allowed-producers:security-service}")
  private String allowedProducersRaw;

  private Set<String> allowedProducers = Set.of();

  @PostConstruct
  void initAllowedProducers() {
    allowedProducers =
        Arrays.stream(allowedProducersRaw.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toSet());
  }

  @KafkaListener(topics = "${kafka.topics.user}", groupId = "${kafka.consumer.group-id}")
  @Transactional
  public void consumeUserCreated(ConsumerRecord<String, String> record) {
    try {
      EventEnvelope<UserCreatedPayload> envelope =
          objectMapper.readValue(record.value(), new TypeReference<>() {});

      if (!EventTypes.USER_CREATED.equals(envelope.eventType())) {
        return;
      }
      if (!isAllowedProducer(envelope.producer())) {
        log.debug("Skipping user.created from producer {}", envelope.producer());
        return;
      }

      UserCreatedPayload payload = envelope.payload();
      if (payload == null
          || payload.userId() == null
          || payload.login() == null
          || payload.name() == null
          || payload.age() == null
          || payload.sex() == null
          || payload.hairColor() == null) {
        log.warn("Invalid user.created event payload, eventId={}", envelope.eventId());
        return;
      }

      ImportUserCommand command =
          new ImportUserCommand(
              payload.userId(),
              payload.login(),
              payload.name(),
              payload.age(),
              toDomainSex(payload.sex()),
              toDomainHairColor(payload.hairColor()));
      importUserUseCase.importUser(command);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to parse user.created event envelope", ex);
    } catch (IllegalStateException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new RuntimeException("Failed to process user.created event", ex);
    }
  }

  private boolean isAllowedProducer(String producer) {
    if (allowedProducers.isEmpty()) {
      return true;
    }
    return producer != null && allowedProducers.contains(producer);
  }

  private static org.nikitakapustkin.domain.enums.Sex toDomainSex(
      org.nikitakapustkin.bank.contracts.enums.Sex sex) {
    if (sex == null) {
      return null;
    }
    return org.nikitakapustkin.domain.enums.Sex.valueOf(sex.name());
  }

  private static org.nikitakapustkin.domain.enums.HairColor toDomainHairColor(
      org.nikitakapustkin.bank.contracts.enums.HairColor hairColor) {
    if (hairColor == null) {
      return null;
    }
    return org.nikitakapustkin.domain.enums.HairColor.valueOf(hairColor.name());
  }
}
