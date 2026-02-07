package org.nikitakapustkin.adapters.out.maintenance;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "maintenance.cleanup")
public class MaintenanceCleanupProperties {
    private long outboxRetentionDays = 7;
    private String cron = "0 0 3 * * *";
}
