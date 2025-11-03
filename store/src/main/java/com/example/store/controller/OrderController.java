package com.example.store.controller;

import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Create a new order
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody @Valid CreateOrderDTO orderDto) {
        OrderDTO order = orderService.placeOrder(orderDto); // triggers Kafka event
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
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Cance
    // a new order
    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber) {
        orderService.requestCancellation(orderNumber); // triggers Kafka event
        return ResponseEntity.accepted().body(Map.of(
                "orderNumber", orderNumber,
                "status", "CANCELLATION_REQUESTED",
                "message", "Order cancellation has been initiated and will be processed shortly."
        ));
    }
}
