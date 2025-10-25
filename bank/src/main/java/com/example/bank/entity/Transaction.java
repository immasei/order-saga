package com.example.bank.entity;

import com.example.bank.enums.AccountType;
import com.example.bank.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String memo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn
    private Account toAccount;         // null for deposits

    @ManyToOne
    @JoinColumn
    private Account fromAccount;       // null for withdrawals

    @ManyToOne(optional = true)
    @JoinColumn(name = "reversal_of_id")
    private Transaction reversalOf;    // null unless reversal

    @Column(nullable = false)
    private boolean reversed = false;  // mark originals when reversed

    @PrePersist
    protected void onCreate() {
        this.time = LocalDateTime.now();
    }

}
