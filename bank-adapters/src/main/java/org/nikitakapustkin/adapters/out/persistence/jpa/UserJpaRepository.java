package org.nikitakapustkin.adapters.out.persistence.jpa;

import org.nikitakapustkin.adapters.out.persistence.jpa.entity.UserEntity;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByLogin(String login);

    Optional<UserEntity> findByLogin(String login);

    List<UserEntity> findByHairColor(HairColor hairColor);
    List<UserEntity> findBySex(Sex sex);
    List<UserEntity> findByHairColorAndSex(HairColor hairColor, Sex sex);

    @Query("SELECT f.id FROM UserEntity u JOIN u.friends f WHERE u.id = :userId")
    List<UUID> findFriendIdsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from UserEntity u where u.id = :userId")
    int deleteExistingById(@Param("userId") UUID userId);
}
