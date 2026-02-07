package org.nikitakapustkin.application.ports.in;

import java.util.UUID;

public interface DeleteUserUseCase {
    void deleteUser(UUID userId);
}
