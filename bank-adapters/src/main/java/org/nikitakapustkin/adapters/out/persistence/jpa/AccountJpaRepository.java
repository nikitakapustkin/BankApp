package org.nikitakapustkin.adapters.out.persistence.jpa;

import org.nikitakapustkin.adapters.out.persistence.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
    List<AccountEntity> findByUser_Id(UUID userId);
}