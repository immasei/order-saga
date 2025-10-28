//package com.example.store.controller;
//
//import com.example.store.model.Order;
//import com.example.store.repository.OrderRepository;
//import com.example.store.service.ProductPurchaseHistoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/orders")
//public class OrderDetailsController {
//
//    @Autowired
//    private OrderRepository orderDetailsRepository;
//
//    @Autowired
//    private ProductPurchaseHistoryService historyService;
//
//    // Get order details by order number
//    @GetMapping("/{orderNumber}")
//    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
//        Optional<Order> order = orderDetailsRepository.findById(UUID.fromString(orderNumber));
//        return order.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // Create a new order
//    @PostMapping
//    public ResponseEntity<Order> createOrder(@RequestBody Order orderDetails) {
//        // Ensure bidirectional link is set
//        orderDetails.getOrderItems().forEach(item -> item.setOrder(orderDetails));
//        Order createdOrder = orderDetailsRepository.save(orderDetails);
//        // Update purchase history for all items
//        historyService.updatePurchaseHistory(createdOrder.getOrderItems());
//        return ResponseEntity.ok(createdOrder);
//    }
//
//    // Update order details
//    @PutMapping("/{id}")
//    public ResponseEntity<Order> updateOrder(@PathVariable UUID id, @RequestBody Order orderDetails) {
//        Optional<Order> existingOrder = orderDetailsRepository.findById(id);
//        if (existingOrder.isPresent()) {
//            Order updatedOrder = existingOrder.get();
//            updatedOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
//            updatedOrder.setStatus(orderDetails.getStatus());
//            updatedOrder.setSubTotal(orderDetails.getSubTotal());
//            updatedOrder.setShipping(orderDetails.getShipping());
//            updatedOrder.setTax(orderDetails.getTax());
//            updatedOrder.setTotal(orderDetails.getTotal());
//            updatedOrder.setPlacedAt(orderDetails.getPlacedAt());
//            orderDetailsRepository.save(updatedOrder);
//            return ResponseEntity.ok(updatedOrder);
//        }
//        return ResponseEntity.notFound().build();
//    }
//
//    // Delete order by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
//        if (orderDetailsRepository.existsById(id)) {
//            orderDetailsRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
//}
