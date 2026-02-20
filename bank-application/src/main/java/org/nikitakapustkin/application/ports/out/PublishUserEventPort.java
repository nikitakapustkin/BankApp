package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.events.DomainEvent;

public interface PublishUserEventPort {
  void publishUserEvent(DomainEvent event);
}
