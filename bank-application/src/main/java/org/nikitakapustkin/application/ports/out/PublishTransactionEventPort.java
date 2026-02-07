package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.events.DomainEvent;

public interface PublishTransactionEventPort {
    void publish(DomainEvent event);
}
