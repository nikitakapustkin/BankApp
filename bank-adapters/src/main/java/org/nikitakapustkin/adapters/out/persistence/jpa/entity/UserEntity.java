package org.nikitakapustkin.adapters.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;

/**
 * Represents a user in the system.
 *
 * <p>This class contains information about the user, including their personal details, friends, and
 * associated accounts.
 */
@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
public class UserEntity {
  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String login;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Sex sex;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private HairColor hairColor;

  @Column(nullable = false)
  private int age;

  @OneToMany(mappedBy = "user")
  private Set<AccountEntity> accounts;

  @ManyToMany
  @JoinTable(
      name = "user_friends",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "friend_id"))
  private Set<UserEntity> friends = new HashSet<>();

  /**
   * Constructs a User object with the specified login, name, sex, hair color, and age.
   *
   * <p>If the age is less than or equal to zero, an IllegalArgumentException is thrown.
   *
   * @param login the login of the user.
   * @param name the name of the user.
   * @param sex the sex of the user.
   * @param hairColor the hair color of the user.
   * @param age the age of the user.
   * @throws IllegalArgumentException if the age is less than or equal to zero.
   */
  public UserEntity(String login, String name, Sex sex, HairColor hairColor, int age) {
    this.login = login;
    this.name = name;
    this.sex = sex;
    this.hairColor = hairColor;
    this.age = age;
    accounts = new HashSet<>();
  }

  /** Adds an account ID to the user's list of associated accounts. */
  public void addAccount(AccountEntity account) {
    this.accounts.add(account);
    account.setUser(this);
  }

  public void addFriend(UserEntity friend) {
    this.friends.add(friend);
    friend.getFriends().add(this);
  }

  public void removeFriend(UserEntity friend) {
    this.friends.remove(friend);
    friend.getFriends().remove(this);
  }
}
