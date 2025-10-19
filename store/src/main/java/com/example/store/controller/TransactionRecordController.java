//package com.example.store.controller;
//
//import com.example.store.model.TransactionRecord;
//import com.example.store.repository.TransactionRecordRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/transactions")
//public class TransactionRecordController {
//
//    @Autowired
//    private TransactionRecordRepository transactionRecordRepository;
//
//    // Get transaction by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<TransactionRecord> getTransactionById(@PathVariable UUID id) {
//        Optional<TransactionRecord> transaction = transactionRecordRepository.findById(id);
//        return transaction.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Get transactions by order ID
//    @GetMapping("/order/{orderId}")
//    public ResponseEntity<TransactionRecord> getTransactionByOrderId(@PathVariable UUID orderId) {
//        return transactionRecordRepository.findAll()
//                .stream()
//                .filter(transaction -> transaction.getOrder().getId().equals(orderId))
//                .findFirst()
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Create a new transaction record
//    @PostMapping
//    public ResponseEntity<TransactionRecord> createTransaction(@RequestBody TransactionRecord transactionRecord) {
//        TransactionRecord createdTransaction = transactionRecordRepository.save(transactionRecord);
//        return ResponseEntity.ok(createdTransaction);
//    }
//
//    // Update transaction record (status and amount)
//    @PutMapping("/{id}")
//    public ResponseEntity<TransactionRecord> updateTransaction(@PathVariable UUID id, @RequestBody TransactionRecord transactionDetails) {
//        Optional<TransactionRecord> existingTransaction = transactionRecordRepository.findById(id);
//        if (existingTransaction.isPresent()) {
//            TransactionRecord transaction = existingTransaction.get();
//            transaction.setStatus(transactionDetails.getStatus());
//            transaction.setAmount(transactionDetails.getAmount());
//            transaction.setUpdatedAt(transactionDetails.getUpdatedAt());
//            transactionRecordRepository.save(transaction);
//            return ResponseEntity.ok(transaction);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // Delete transaction by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
//        if (transactionRecordRepository.existsById(id)) {
//            transactionRecordRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//}
