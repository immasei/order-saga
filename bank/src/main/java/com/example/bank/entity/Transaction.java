package com.example.bank.entity;

import com.example.bank.enums.TransactionType;
import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(
    name = "transactions",
    indexes = {
        @Index(name = "idx_transaction_ref", columnList = "transactionRef"),
        @Index(name = "idx_transaction_type", columnList = "transactionType")
    }
)
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 30, unique = true, nullable = false, updatable = false)
    private String transactionRef;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    private String memo;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

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

    @Column(length=80, updatable=false, unique=true)
    private String idempotencyKey;

    @PrePersist
    protected void onCreate() {
        generateTransactionRef();
    }

    private void generateTransactionRef() {
        if (this.transactionRef == null) {
            this.transactionRef = "TX-" + UlidCreator.getMonotonicUlid().toString();
        }
    }

}
