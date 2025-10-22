package com.example.store.model.account;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an admin account (no extra fields for now).
 */
@Entity
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class Admin extends User {
    // No extra fields yet -> inherits everything from UserAccount
}