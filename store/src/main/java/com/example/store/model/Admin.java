package com.example.store.model;

import com.example.store.model.enums.UserRole;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Admin extends UserAccount {
    public Admin(String email, String passwordHash, UserRole role, String firstName, String lastName) {
        super(email, passwordHash, role, firstName, lastName);
    }
}