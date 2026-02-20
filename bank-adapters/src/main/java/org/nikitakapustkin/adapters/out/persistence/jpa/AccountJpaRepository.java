package org.nikitakapustkin.adapters.out.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
  List<AccountEntity> findByUser_Id(UUID userId);
}
