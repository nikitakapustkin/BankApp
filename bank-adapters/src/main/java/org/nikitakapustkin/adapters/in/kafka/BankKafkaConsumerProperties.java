package org.nikitakapustkin.adapters.in.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.consumer")
public class BankKafkaConsumerProperties {
  private String groupId;
  private String dltSuffix = ".dlt";
  private long backoffMs = 1000;
  private long maxRetries = 3;
}
