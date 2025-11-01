package com.example.bank.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "customers",
    indexes = @Index(name = "idx_customer_ref", columnList = "customerRef", unique = true)
)
public class Customer {

    @Id
    @GeneratedValue
    private long id;

    @Column(length = 30, unique = true, nullable = false, updatable = false)
    private String customerRef;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @OneToMany(mappedBy = "accountHolder")
    private final Collection<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        this.accounts.add(account);
        account.setAccountHolder(this);
    }

    @PrePersist
    private void generateCustomerRef() {
        if (this.customerRef == null) {
            this.customerRef = "CLI-" + UlidCreator.getMonotonicUlid().toString();
        }
    }

}
