//package com.example.store.controller;
//
//import com.example.store.model.ProductPurchaseHistory;
//import com.example.store.repository.ProductPurchaseHistoryRepository;
//import com.example.store.repository.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/product-purchase-history")
//public class ProductPurchaseHistoryController {
//
//    @Autowired
//    private ProductPurchaseHistoryRepository productPurchaseHistoryRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    // Create a new product purchase history
//    @PostMapping
//    public ResponseEntity<ProductPurchaseHistory> createProductPurchaseHistory(@RequestBody ProductPurchaseHistory history) {
//        // Ensure the product exists before creating purchase history
//        if (!productRepository.existsById(history.getProduct().getId())) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        ProductPurchaseHistory savedHistory = productPurchaseHistoryRepository.save(history);
//        return new ResponseEntity<>(savedHistory, HttpStatus.CREATED);
//    }
//
//    // Get product purchase history by product ID
//    @GetMapping("/{productId}")
//    public ResponseEntity<ProductPurchaseHistory> getPurchaseHistoryByProductId(@PathVariable UUID productId) {
//        Optional<ProductPurchaseHistory> purchaseHistory = productPurchaseHistoryRepository.findById(productId);
//        return purchaseHistory.map(productPurchaseHistory -> new ResponseEntity<>(productPurchaseHistory, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }
//
//    // Update purchase count for a product
//    @PutMapping("/{productId}")
//    public ResponseEntity<ProductPurchaseHistory> updatePurchaseCount(@PathVariable UUID productId, @RequestBody int purchaseCount) {
//        Optional<ProductPurchaseHistory> existingHistory = productPurchaseHistoryRepository.findById(productId);
//        if (existingHistory.isPresent()) {
//            ProductPurchaseHistory history = existingHistory.get();
//            history.setPurchaseCount(purchaseCount);
//            productPurchaseHistoryRepository.save(history);
//            return new ResponseEntity<>(history, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    // Delete product purchase history by product ID
//    @DeleteMapping("/{productId}")
//    public ResponseEntity<Void> deletePurchaseHistory(@PathVariable UUID productId) {
//        if (productPurchaseHistoryRepository.existsById(productId)) {
//            productPurchaseHistoryRepository.deleteById(productId);
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//}
