//package com.example.store.controller;
//
//import com.example.store.model.DeliveryRecord;
//import com.example.store.repository.DeliveryRecordRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/delivery")
//public class DeliveryRecordController {
//
//    @Autowired
//    private DeliveryRecordRepository deliveryRecordRepository;
//
//    // Get delivery record by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<DeliveryRecord> getDeliveryById(@PathVariable UUID id) {
//        Optional<DeliveryRecord> deliveryRecord = deliveryRecordRepository.findById(id);
//        return deliveryRecord.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Get delivery records by order ID
//    @GetMapping("/order/{orderId}")
//    public ResponseEntity<DeliveryRecord> getDeliveryByOrderId(@PathVariable UUID orderId) {
//        return deliveryRecordRepository.findAll()
//                .stream()
//                .filter(delivery -> delivery.getOrder().getId().equals(orderId))
//                .findFirst()
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Create a new delivery record
//    @PostMapping
//    public ResponseEntity<DeliveryRecord> createDelivery(@RequestBody DeliveryRecord deliveryRecord) {
//        DeliveryRecord createdDelivery = deliveryRecordRepository.save(deliveryRecord);
//        return ResponseEntity.ok(createdDelivery);
//    }
//
//    // Update delivery record (status, shipped_at, delivered_at)
//    @PutMapping("/{id}")
//    public ResponseEntity<DeliveryRecord> updateDelivery(@PathVariable UUID id, @RequestBody DeliveryRecord deliveryDetails) {
//        Optional<DeliveryRecord> existingDelivery = deliveryRecordRepository.findById(id);
//        if (existingDelivery.isPresent()) {
//            DeliveryRecord delivery = existingDelivery.get();
//            delivery.setStatus(deliveryDetails.getStatus());
//            delivery.setShippedAt(deliveryDetails.getShippedAt());
//            delivery.setDeliveredAt(deliveryDetails.getDeliveredAt());
//            deliveryRecordRepository.save(delivery);
//            return ResponseEntity.ok(delivery);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // Delete delivery record by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteDelivery(@PathVariable UUID id) {
//        if (deliveryRecordRepository.existsById(id)) {
//            deliveryRecordRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//}
