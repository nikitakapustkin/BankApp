package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.events.DomainEvent;

public interface PublishAccountEventPort {
    void publishAccountEvent(DomainEvent event);
}
