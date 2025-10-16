package com.example.store.controller;

import com.example.store.model.Warehouse;
import com.example.store.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseRepository warehouseRepository;

    // Get warehouse by ID
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable UUID id) {
        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
        return warehouse.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new warehouse
    @PostMapping
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse) {
        Warehouse createdWarehouse = warehouseRepository.save(warehouse);
        return ResponseEntity.ok(createdWarehouse);
    }

    // Update warehouse details
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable UUID id, @RequestBody Warehouse warehouseDetails) {
        Optional<Warehouse> existingWarehouse = warehouseRepository.findById(id);
        if (existingWarehouse.isPresent()) {
            Warehouse warehouse = existingWarehouse.get();
            warehouse.setName(warehouseDetails.getName());
            warehouse.setLocation(warehouseDetails.getLocation());
            warehouseRepository.save(warehouse);
            return ResponseEntity.ok(warehouse);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete warehouse by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable UUID id) {
        if (warehouseRepository.existsById(id)) {
            warehouseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
