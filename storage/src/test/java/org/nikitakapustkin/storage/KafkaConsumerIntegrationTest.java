package org.nikitakapustkin.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.bank.contracts.events.AccountDepositedPayload;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.TransactionCreatedPayload;
import org.nikitakapustkin.bank.contracts.events.UserCreatedPayload;
import org.nikitakapustkin.storage.adapters.out.persistence.AccountEventRepository;
import org.nikitakapustkin.storage.adapters.out.persistence.TransactionEventRepository;
import org.nikitakapustkin.storage.adapters.out.persistence.UserEventRepository;
import org.nikitakapustkin.storage.events.AccountEvent;
import org.nikitakapustkin.storage.events.TransactionEvent;
import org.nikitakapustkin.storage.events.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    properties = {
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "spring.datasource.url=jdbc:h2:mem:storagetest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=true",
      "spring.flyway.locations=classpath:db/migration/h2",
      "kafka.topics.user=client-topic",
      "kafka.topics.account=account-topic",
      "kafka.topics.transaction=transaction-topic",
      "kafka.consumer.group-id=storage-test-group"
    })
@EmbeddedKafka(
    partitions = 1,
    topics = {"client-topic", "account-topic", "transaction-topic"})
@DirtiesContext
class KafkaConsumerIntegrationTest {

  @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;
  @Autowired UserEventRepository userEventRepository;
  @Autowired AccountEventRepository accountEventRepository;
  @Autowired TransactionEventRepository transactionEventRepository;
  @Autowired KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  private KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void setUp() {
    Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    kafkaTemplate.setDefaultTopic("client-topic");

    kafkaListenerEndpointRegistry
        .getListenerContainers()
        .forEach(
            container ->
                ContainerTestUtils.waitForAssignment(
                    container, embeddedKafkaBroker.getPartitionsPerTopic()));
  }

  @Test
  void consumes_user_event_from_kafka_and_persists() throws Exception {
    UUID eventId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<UserCreatedPayload> envelope =
        new EventEnvelope<>(
            eventId,
            EventTypes.USER_CREATED,
            eventTime,
            correlationId,
            "bank-service",
            new UserCreatedPayload(
                userId, "alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK, "User created"));
    kafkaTemplate
        .send("client-topic", userId.toString(), objectMapper.writeValueAsString(envelope))
        .get(5, TimeUnit.SECONDS);

    UserEvent saved = awaitUserEvent(eventId);
    assertThat(saved).isNotNull();
    assertThat(saved.getEventId()).isEqualTo(eventId);
    assertThat(saved.getCorrelationId()).isEqualTo(correlationId);
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getEventType()).isEqualTo(EventTypes.USER_CREATED);
    assertThat(saved.getEventTime()).isEqualTo(eventTime);
    assertThat(saved.getEventDescription()).isEqualTo("User created");
    assertThat(saved.getPayloadType()).isEqualTo(UserCreatedPayload.class.getName());
    UserCreatedPayload actualPayload =
        objectMapper.readValue(saved.getPayload(), UserCreatedPayload.class);
    assertThat(actualPayload).isEqualTo(envelope.payload());
  }

  @Test
  void consumes_account_event_from_kafka_and_persists() throws Exception {
    UUID eventId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<AccountDepositedPayload> envelope =
        new EventEnvelope<>(
            eventId,
            EventTypes.ACCOUNT_DEPOSIT,
            eventTime,
            null,
            "bank-service",
            new AccountDepositedPayload(accountId, new BigDecimal("10.00"), "Deposit"));
    kafkaTemplate
        .send("account-topic", accountId.toString(), objectMapper.writeValueAsString(envelope))
        .get(5, TimeUnit.SECONDS);

    AccountEvent saved = awaitAccountEvent(eventId);
    assertThat(saved).isNotNull();
    assertThat(saved.getEventId()).isEqualTo(eventId);
    assertThat(saved.getCorrelationId()).isEqualTo(eventId);
    assertThat(saved.getAccountId()).isEqualTo(accountId);
    assertThat(saved.getEventType()).isEqualTo(EventTypes.ACCOUNT_DEPOSIT);
    assertThat(saved.getEventTime()).isEqualTo(eventTime);
    assertThat(saved.getEventDescription()).isEqualTo("Deposit");
    assertThat(saved.getPayloadType()).isEqualTo(AccountDepositedPayload.class.getName());
    AccountDepositedPayload actualPayload =
        objectMapper.readValue(saved.getPayload(), AccountDepositedPayload.class);
    assertThat(actualPayload.accountId()).isEqualTo(accountId);
    assertThat(actualPayload.amount()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(actualPayload.description()).isEqualTo("Deposit");
  }

  @Test
  void consumes_transaction_event_from_kafka_and_persists() throws Exception {
    UUID transactionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<TransactionCreatedPayload> envelope =
        new EventEnvelope<>(
            transactionId,
            EventTypes.TRANSACTION_CREATED,
            createdAt,
            correlationId,
            "bank-service",
            new TransactionCreatedPayload(
                transactionId,
                accountId,
                TransactionType.WITHDRAWAL,
                new BigDecimal("25.00"),
                createdAt));
    kafkaTemplate
        .send("transaction-topic", accountId.toString(), objectMapper.writeValueAsString(envelope))
        .get(5, TimeUnit.SECONDS);

    TransactionEvent saved = awaitTransactionEvent(transactionId);
    assertThat(saved).isNotNull();
    assertThat(saved.getEventId()).isEqualTo(transactionId);
    assertThat(saved.getTransactionId()).isEqualTo(transactionId);
    assertThat(saved.getCorrelationId()).isEqualTo(correlationId);
    assertThat(saved.getAccountId()).isEqualTo(accountId);
    assertThat(saved.getEventType()).isEqualTo(EventTypes.TRANSACTION_CREATED);
    assertThat(saved.getEventTime()).isEqualTo(createdAt);
    assertThat(saved.getEventDescription()).isNull();
    assertThat(saved.getPayloadType()).isEqualTo(TransactionCreatedPayload.class.getName());
    assertThat(saved.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
    assertThat(saved.getAmount()).isEqualTo(new BigDecimal("25.00"));
    assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
    TransactionCreatedPayload actualPayload =
        objectMapper.readValue(saved.getPayload(), TransactionCreatedPayload.class);
    assertThat(actualPayload.transactionId()).isEqualTo(transactionId);
    assertThat(actualPayload.accountId()).isEqualTo(accountId);
    assertThat(actualPayload.transactionType()).isEqualTo(TransactionType.WITHDRAWAL);
    assertThat(actualPayload.amount()).isEqualByComparingTo(new BigDecimal("25.00"));
    assertThat(actualPayload.createdAt()).isEqualTo(createdAt);
  }

  private UserEvent awaitUserEvent(UUID eventId) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 10000;
    UserEvent found = null;
    while (System.currentTimeMillis() < deadline && found == null) {
      found =
          userEventRepository.findAll().stream()
              .filter(e -> eventId.equals(e.getEventId()))
              .findFirst()
              .orElse(null);
      if (found == null) {
        Thread.sleep(100);
      }
    }
    return found;
  }

  private AccountEvent awaitAccountEvent(UUID eventId) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 10000;
    AccountEvent found = null;
    while (System.currentTimeMillis() < deadline && found == null) {
      found =
          accountEventRepository.findAll().stream()
              .filter(e -> eventId.equals(e.getEventId()))
              .findFirst()
              .orElse(null);
      if (found == null) {
        Thread.sleep(100);
      }
    }
    return found;
  }

  private TransactionEvent awaitTransactionEvent(UUID transactionId) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 10000;
    TransactionEvent found = null;
    while (System.currentTimeMillis() < deadline && found == null) {
      found =
          transactionEventRepository.findAll().stream()
              .filter(e -> transactionId.equals(e.getTransactionId()))
              .findFirst()
              .orElse(null);
      if (found == null) {
        Thread.sleep(100);
      }
    }
    return found;
  }
}
