package org.nikitakapustkin.adapters.out.persistence;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.adapters.out.persistence.jpa.UserJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.UserEntity;
import org.nikitakapustkin.adapters.out.persistence.mapper.UserMapper;
import org.nikitakapustkin.application.ports.out.CreateUserPort;
import org.nikitakapustkin.application.ports.out.DeleteUserPort;
import org.nikitakapustkin.application.ports.out.LoadFriendsPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.LoadUsersPort;
import org.nikitakapustkin.application.ports.out.UpdateFriendsPort;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.nikitakapustkin.domain.models.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter
    implements LoadUserPort,
        LoadUsersPort,
        LoadFriendsPort,
        CreateUserPort,
        UpdateFriendsPort,
        DeleteUserPort {

  private final UserJpaRepository users;
  private final EntityManager entityManager;

  @Override
  @Transactional(readOnly = true)
  public Optional<User> loadUserByLogin(String login) {
    return users.findByLogin(login).map(UserMapper::toDomain);
  }

  @Override
  public Optional<User> loadUserById(UUID id) {
    return users.findById(id).map(UserMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UUID> loadFriendsIds(UUID userId) {
    return users.findFriendIdsByUserId(userId);
  }

  @Override
  @Transactional
  public User create(User user) {
    var entity = UserMapper.toJpaEntity(user);
    if (entity.getId() == null) {
      entity.setId(UUID.randomUUID());
    }
    entityManager.persist(entity);
    return UserMapper.toDomain(entity);
  }

  @Override
  @Transactional
  public boolean deleteById(UUID userId) {
    if (userId == null) {
      return false;
    }
    int deleted = users.deleteExistingById(userId);
    entityManager.clear();
    return deleted > 0;
  }

  @Override
  @Transactional
  public void addFriend(UUID userId, UUID friendId) {
    var user =
        users
            .findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    var friend =
        users
            .findById(friendId)
            .orElseThrow(() -> new IllegalStateException("Friend not found: " + friendId));

    user.addFriend(friend);
    users.save(user);
  }

  @Override
  @Transactional
  public void removeFriend(UUID userId, UUID friendId) {
    var user =
        users
            .findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    var friend =
        users
            .findById(friendId)
            .orElseThrow(() -> new IllegalStateException("Friend not found: " + friendId));

    user.removeFriend(friend);
    users.save(user);
  }

  @Override
  public List<User> findAllByFilters(HairColor hairColor, Sex sex) {
    List<UserEntity> entities;

    if (hairColor != null && sex != null) {
      entities = users.findByHairColorAndSex(hairColor, sex);
    } else if (hairColor != null) {
      entities = users.findByHairColor(hairColor);
    } else if (sex != null) {
      entities = users.findBySex(sex);
    } else {
      entities = users.findAll();
    }

    return entities.stream().map(UserMapper::toDomain).toList();
  }
}
