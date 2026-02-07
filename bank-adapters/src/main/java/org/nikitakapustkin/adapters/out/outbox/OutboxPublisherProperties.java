package org.nikitakapustkin.adapters.out.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "outbox.publisher")
public class OutboxPublisherProperties {
    private int batchSize = 100;
    private long publishTimeoutMs = 5000;
    private int maxAttempts = 5;
    private long intervalMs = 1000;
}
