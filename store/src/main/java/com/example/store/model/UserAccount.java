package com.example.store.model;

import com.example.store.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@Getter
@Setter
public abstract class UserAccount {
    /*
    * This is an abstract class. All customers are users but not all users
    * are customers, so customer inherits from user.
    * The inheritance strategy is JOINED, meaning there is one table
    * per class (i.e. The User table holds common fields. The Customer
    * table holds customer-specific fields and a foreign key to the
    * User table (which also acts as its primary key).
    * */

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING) // Enums found in the enums package, we have ADMIN, CUSTOMER, and GUEST
    private UserRole role;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    // Constructor
    public UserAccount(String email, String passwordHash, UserRole role, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Debugging method
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
