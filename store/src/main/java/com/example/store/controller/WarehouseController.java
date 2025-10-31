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

    @GetMapping("/reservations/{orderNumber}")
    public ResponseEntity<InventoryAllocationDTO> getReservation(@PathVariable String orderNumber) {
        InventoryAllocationDTO reservation = warehouseService.getReservation(orderNumber);
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations/{orderNumber}/release")
    public ResponseEntity<InventoryAllocationDTO> releaseReservation(@PathVariable String orderNumber,
                                                                     @RequestBody @Valid ReleaseReservationRequest request) {
        InventoryAllocationDTO reservation = warehouseService.releaseReservation(orderNumber, request.reason());
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations/{orderNumber}/commit")
    public ResponseEntity<InventoryAllocationDTO> commitReservation(@PathVariable String orderNumber) {
        InventoryAllocationDTO reservation = warehouseService.commitReservation(orderNumber);
        return ResponseEntity.ok(reservation);
    }

    // Get warehouse by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable UUID id) {
//        Optional<Warehouse> warehouse = warehouseRepository.findById(id);
//        return warehouse.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Update warehouse details
//    @PutMapping("/{id}")
//    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable UUID id, @RequestBody Warehouse warehouseDetails) {
//        Optional<Warehouse> existingWarehouse = warehouseRepository.findById(id);
//        if (existingWarehouse.isPresent()) {
//            Warehouse warehouse = existingWarehouse.get();
//            warehouse.setName(warehouseDetails.getName());
//            warehouse.setLocation(warehouseDetails.getLocation());
//            warehouseRepository.save(warehouse);
//            return ResponseEntity.ok(warehouse);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // Delete warehouse by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteWarehouse(@PathVariable UUID id) {
//        if (warehouseRepository.existsById(id)) {
//            warehouseRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
}
