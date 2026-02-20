package org.nikitakapustkin.storage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikitakapustkin.storage.adapters.out.persistence.AccountEventRepository;
import org.nikitakapustkin.storage.adapters.out.persistence.TransactionEventRepository;
import org.nikitakapustkin.storage.adapters.out.persistence.UserEventRepository;
import org.nikitakapustkin.storage.application.EventIngestionService;
import org.nikitakapustkin.storage.application.EventQueryService;
import org.nikitakapustkin.storage.application.ports.out.AccountEventRepositoryPort;
import org.nikitakapustkin.storage.application.ports.out.TransactionEventRepositoryPort;
import org.nikitakapustkin.storage.application.ports.out.UserEventRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServiceConfig {
  @Bean
  public EventQueryService eventQueryService(
      UserEventRepository userEventRepository,
      AccountEventRepository accountEventRepository,
      TransactionEventRepository transactionEventRepository) {
    return new EventQueryService(
        userEventRepository, accountEventRepository, transactionEventRepository);
  }

  @Bean
  public EventIngestionService eventIngestionService(
      UserEventRepositoryPort userEventRepository,
      AccountEventRepositoryPort accountEventRepository,
      TransactionEventRepositoryPort transactionEventRepository,
      ObjectMapper objectMapper) {
    return new EventIngestionService(
        userEventRepository, accountEventRepository, transactionEventRepository, objectMapper);
  }
}
