package com.example.store.controller;

import com.example.store.dto.inventory.InventoryAllocationDTO;
import com.example.store.dto.inventory.ReleaseReservationRequest;
import com.example.store.kafka.command.ReleaseInventory;
import com.example.store.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/{orderNumber}")
    public ResponseEntity<InventoryAllocationDTO> getReservation(@PathVariable String orderNumber) {
        InventoryAllocationDTO reservation = reservationService.getReservation(orderNumber);
        return ResponseEntity.ok(reservation);
    }

//    @PostMapping("/{orderNumber}/release")
//    public ResponseEntity<InventoryAllocationDTO> releaseReservation(@PathVariable String orderNumber,
//                                                                     @RequestBody @Valid ReleaseReservationRequest request) {
//        InventoryAllocationDTO reservation = reservationService
//                .releaseReservation(ReleaseInventory.of(orderNumber, request.idempotencyKey(), request.reason()));
//        return ResponseEntity.ok(reservation);
//    }

    @PostMapping("/{orderNumber}/commit")
    public ResponseEntity<InventoryAllocationDTO> commitReservation(@PathVariable String orderNumber) {
        InventoryAllocationDTO reservation = reservationService.commitReservation(orderNumber);
        return ResponseEntity.ok(reservation);
    }
}
