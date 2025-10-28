package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a customer account with extra contact information.
 */
@Entity
@DiscriminatorValue("CUSTOMER")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User {

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

}
