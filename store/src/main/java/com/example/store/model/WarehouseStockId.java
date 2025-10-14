package com.example.store.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WarehouseStockId implements Serializable {
    private UUID warehouseId;
    private UUID productId;
}