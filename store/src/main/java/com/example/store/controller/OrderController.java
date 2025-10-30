package com.example.store.controller;

import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.kafka.saga.OrderHandler;
import com.example.store.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderHandler orderHandler;

    // Create a new order
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid CreateOrderDTO orderDto) {
        OrderDTO order = orderHandler.placeOrder(orderDto);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    // Get order details by order number
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getALlOrders();
        return ResponseEntity.ok(orders);
    }

    // Update order details
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

    // Delete order by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
//        if (orderDetailsRepository.existsById(id)) {
//            orderDetailsRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.notFound().build();
//    }
}
