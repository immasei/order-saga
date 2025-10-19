package com.example.store.model;

import com.example.store.model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "user_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Customer.class, name = "CUSTOMER"),
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN")
})
public abstract class UserAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    public UserAccount(String email, String passwordHash, UserRole role, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
