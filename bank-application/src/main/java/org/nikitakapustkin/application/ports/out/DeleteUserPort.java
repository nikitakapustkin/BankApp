package org.nikitakapustkin.application.ports.out;

import java.util.UUID;

public interface DeleteUserPort {
  boolean deleteById(UUID userId);
}
