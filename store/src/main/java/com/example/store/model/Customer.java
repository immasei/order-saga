package com.example.store.model;

import com.example.store.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Customer extends UserAccount {

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    // Constructor
    public Customer(String email, String passwordHash, UserRole role, String firstName, String lastName, String phone, String address) {
        super(email, passwordHash, role, firstName, lastName);
        this.phone = phone;
        this.address = address;
    }

    // Debugging method
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", role='" + getRole() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
