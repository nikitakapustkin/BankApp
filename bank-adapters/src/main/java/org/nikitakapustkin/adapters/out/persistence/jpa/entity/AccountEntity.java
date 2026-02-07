package org.nikitakapustkin.adapters.out.persistence.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {

    @Version
    @Column(nullable = false)
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", columnDefinition = "uuid")
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "owner_login", nullable = false)
    private String ownerLogin;

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactions = new ArrayList<>();
}
