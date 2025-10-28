//package com.example.store.controller;
//
//import com.example.store.dto.account.CreateCustomerDTO;
//import com.example.store.dto.account.UserDTO;
//import com.example.store.dto.order.CreateOrderDTO;
//import com.example.store.service.OrderService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/orders")
//@RequiredArgsConstructor
//public class OrderController {
//
//    private final OrderService orderService;
//
//    @PostMapping
//    public ResponseEntity<UserDTO> placeOrder(@RequestBody CreateOrderDTO orderDto) {
//        UserDTO customer = orderService.startOrder(orderDto);
//        return new ResponseEntity<>(customer, HttpStatus.CREATED);
//    }
//
//}
