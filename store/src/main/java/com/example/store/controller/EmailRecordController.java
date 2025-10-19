//package com.example.store.controller;
//
//import com.example.store.model.EmailRecord;
//import com.example.store.repository.EmailRecordRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/emails")
//public class EmailRecordController {
//
//    @Autowired
//    private EmailRecordRepository emailRecordRepository;
//
//    // Get email record by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<EmailRecord> getEmailById(@PathVariable UUID id) {
//        Optional<EmailRecord> emailRecord = emailRecordRepository.findById(id);
//        return emailRecord.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Get email records by order ID
//    @GetMapping("/order/{orderId}")
//    public ResponseEntity<EmailRecord> getEmailByOrderId(@PathVariable UUID orderId) {
//        return emailRecordRepository.findAll()
//                .stream()
//                .filter(email -> email.getOrder().getId().equals(orderId))
//                .findFirst()
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Create a new email record
//    @PostMapping
//    public ResponseEntity<EmailRecord> createEmail(@RequestBody EmailRecord emailRecord) {
//        EmailRecord createdEmail = emailRecordRepository.save(emailRecord);
//        return ResponseEntity.ok(createdEmail);
//    }
//
//    // Update email record (status)
//    @PutMapping("/{id}")
//    public ResponseEntity<EmailRecord> updateEmail(@PathVariable UUID id, @RequestBody EmailRecord emailDetails) {
//        Optional<EmailRecord> existingEmail = emailRecordRepository.findById(id);
//        if (existingEmail.isPresent()) {
//            EmailRecord email = existingEmail.get();
//            email.setStatus(emailDetails.getStatus());
//            emailRecordRepository.save(email);
//            return ResponseEntity.ok(email);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//}
