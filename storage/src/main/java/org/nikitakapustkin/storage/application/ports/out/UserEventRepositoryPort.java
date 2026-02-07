package org.nikitakapustkin.storage.application.ports.out;

import org.nikitakapustkin.storage.events.UserEvent;

public interface UserEventRepositoryPort {
    UserEvent save(UserEvent event);
}
