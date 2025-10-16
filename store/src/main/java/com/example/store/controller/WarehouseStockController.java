package com.example.store.controller;

import com.example.store.model.Warehouse1Stock;
import com.example.store.model.Warehouse2Stock;
import com.example.store.repository.Warehouse1StockRepository;
import com.example.store.repository.Warehouse2StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/warehouse-stocks")
public class WarehouseStockController {

    @Autowired
    private Warehouse1StockRepository warehouse1StockRepository;

    @Autowired
    private Warehouse2StockRepository warehouse2StockRepository;

    // Get stock information for Warehouse 1
    @GetMapping("/warehouse1/{warehouseId}/product/{productId}")
    public ResponseEntity<Warehouse1Stock> getWarehouse1Stock(@PathVariable UUID warehouseId, @PathVariable UUID productId) {
        Warehouse1Stock stock = (Warehouse1Stock) warehouse1StockRepository.findById(new UUID[]{warehouseId, productId}).orElse(null);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get stock information for Warehouse 2
    @GetMapping("/warehouse2/{warehouseId}/product/{productId}")
    public ResponseEntity<Warehouse2Stock> getWarehouse2Stock(@PathVariable UUID warehouseId, @PathVariable UUID productId) {
        Warehouse2Stock stock = (Warehouse2Stock) warehouse2StockRepository.findById(new UUID[]{warehouseId, productId}).orElse(null);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create or update stock for Warehouse 1
    @PostMapping("/warehouse1")
    public ResponseEntity<Warehouse1Stock> createWarehouse1Stock(@RequestBody Warehouse1Stock warehouse1Stock) {
        Warehouse1Stock createdStock = warehouse1StockRepository.save(warehouse1Stock);
        return ResponseEntity.ok(createdStock);
    }

    // Create or update stock for Warehouse 2
    @PostMapping("/warehouse2")
    public ResponseEntity<Warehouse2Stock> createWarehouse2Stock(@RequestBody Warehouse2Stock warehouse2Stock) {
        Warehouse2Stock createdStock = warehouse2StockRepository.save(warehouse2Stock);
        return ResponseEntity.ok(createdStock);
    }
}
