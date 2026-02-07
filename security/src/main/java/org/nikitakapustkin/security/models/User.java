package org.nikitakapustkin.security.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nikitakapustkin.security.enums.Role;

import java.util.UUID;

@Entity
@Table(name = "security_users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;
    @Column(nullable = false, unique = true)
    private String login;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public User(String login, String password, Role role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }
}
