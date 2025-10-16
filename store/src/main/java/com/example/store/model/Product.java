package com.example.store.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.Collection;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private long price;

    public Product(UUID id, String name, String description, long priceCents) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = priceCents;
    }

    //  Method for debugging
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priceCents=" + price +
                '}';
    }
}
