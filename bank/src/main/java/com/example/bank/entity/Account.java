package com.example.bank.entity;
import com.example.bank.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false, precision = 10, scale = 2) // 16 digits before decimal, 2 after
    private BigDecimal balance = BigDecimal.ZERO.setScale(2);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

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
}
