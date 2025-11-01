package com.example.store.controller;

import com.example.store.dto.inventory.CreateWarehouseDTO;
import com.example.store.dto.inventory.InventoryAllocationDTO;
import com.example.store.dto.inventory.ReleaseReservationRequest;
import com.example.store.dto.inventory.StockDTO;
import com.example.store.dto.inventory.WarehouseDTO;
import com.example.store.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // Create a new warehouse
    @PostMapping
    public ResponseEntity<WarehouseDTO> createWarehouse(@RequestBody @Valid CreateWarehouseDTO warehouseDto) {
        WarehouseDTO warehouse = warehouseService.createWarehouse(warehouseDto);
        return new ResponseEntity<>(warehouse, HttpStatus.CREATED);
    }

    // Create new warehouses in batch
    @PostMapping("/batch")
    public ResponseEntity<List<WarehouseDTO>> createWarehouses(@RequestBody @Valid List<CreateWarehouseDTO> warehouseDtos) {
        List<WarehouseDTO> warehouses = warehouseService.createWarehousesInBatch(warehouseDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouses);
    }

    @GetMapping
    public ResponseEntity<List<WarehouseDTO>> getAllWarehouses() {
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/{warehouseCode}")
    public ResponseEntity<WarehouseDTO> getWarehouseByCode(@PathVariable String warehouseCode) {
        WarehouseDTO warehouse = warehouseService.getWarehouseByCode(warehouseCode);
        return ResponseEntity.ok(warehouse);
    }

    @GetMapping("/{warehouseCode}/stocks")
    public ResponseEntity<List<StockDTO>> getStocksByWarehouse(@PathVariable String warehouseCode) {
        List<StockDTO> stocks = warehouseService.getStocksByWarehouseCode(warehouseCode); // upsert
        return ResponseEntity.ok(stocks);
    }

}
