package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
    name = "warehouses",
    indexes = @Index(name = "idx_warehouse_code", columnList = "warehouseCode", unique = true)
)
public class Warehouse {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 20, nullable = false, unique = true)
    private String warehouseCode; // ie SYD-01

    @Column(length = 100, nullable = false)
    private String warehouseName;

    @Column(length = 100, nullable = false)
    private String location;

}
