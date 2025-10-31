package com.example.bank.entity;
import com.example.bank.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.github.f4b6a3.ulid.UlidCreator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "accounts",
    indexes = {
        @Index(name = "ix_account_customer", columnList = "account_holder_id"),
        @Index(name = "ux_account_ref",     columnList = "account_ref", unique = true)
    }
)
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 30, unique = true, nullable = false, updatable = false)
    private String accountRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Customer accountHolder;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO.setScale(2);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType = AccountType.PERSONAL;

    @Version
    private int version;

    @OneToMany(mappedBy = "fromAccount")
    private final Collection<Transaction> outgoingTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "toAccount")
    private final Collection<Transaction> incomingTransactions = new ArrayList<>();

    public void modifyBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public void addIncomingTransaction(Transaction transaction) {
        this.incomingTransactions.add(transaction);
    }

    public void addOutgoingTransaction(Transaction transaction) {
        this.outgoingTransactions.add(transaction);
    }

    @PrePersist
    private void onCreate() {
        generateOrderNumber();
    }

    private void generateOrderNumber() {
        if (this.accountRef == null) {
            this.accountRef = "BAC-" + UlidCreator.getMonotonicUlid().toString();
        }
    }
}
