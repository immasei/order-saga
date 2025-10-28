package com.example.store.service;
//
//import com.example.store.dto.order.CreateOrderDTO;
//
//// SAGA orchestrator
//public class OrderService {
//

import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderItemDTO;
import com.example.store.model.Customer;
import com.example.store.model.Order;
import com.example.store.repository.UserRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

////    public void start(OrderPlaced placed) {
////        state.putIfAbsent(placed.orderId(), new SagaState(placed.orderId(), placed.amountCents()));
////        // Step 1: reserve inventory
////        commands.reserveInventory(new ReserveInventoryCommand(placed.orderId()));
////        // optional: send user “received” email
////    }
//
//    // start order SAGA
//    public void createOrder(CreateOrderDTO orderDto) {
//        // Step 1: Save the order to the database with PENDING status (Local Transaction)
//        // orderRepository.save(new Order(request.getOrderId(), "PENDING"));
//
//        // Step 2: Send 'Deduct Stock' message to Inventory Service
//        System.out.println("Sending 'Deduct Stock' message to Inventory Service...");
//        rabbitTemplate.convertAndSend("inventory.queue", request);
//    }
//
//}

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void createOrder(CreateOrderDTO orderDto) {
        Customer customer = (Customer) userRepository.getOrThrow(orderDto.getCustomerId());

        Order newOrder = toEntity(orderDto);

        for (OrderItemDTO item : orderDto.getOrderItems()) {

        }
//        Product product = productRepository
//                .findByProductCodeOrThrow(orderDto.g)
//        Order orfer = customerRepository.getOrThrow(customerId);
//        customer.addAccount(newAccount);
//
//        Account saved = accountRepository.saveAndFlush(newAccount);
//        return toResponse(saved);

//        // Step 1: Save the order to the database with PENDING status (Local Transaction)
//        // orderRepository.save(new Order(request.getOrderId(), "PENDING"));
//
//        // Step 2: Send 'Deduct Stock' message to Inventory Service
//        System.out.println("Sending 'Deduct Stock' message to Inventory Service...");
//        rabbitTemplate.convertAndSend("inventory.queue", request);
    }

    public Order toEntity(CreateOrderDTO dto) {
        return modelMapper.map(dto, Order.class);
    }

}
