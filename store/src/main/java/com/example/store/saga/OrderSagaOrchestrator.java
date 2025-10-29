package com.example.store.saga;

import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.service.OrderService;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
//
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {
//
    private final OrderService orderService;
//
//    // === Entry point: user placed order ===
    @Transactional
    public OrderDTO placeOrder(CreateOrderDTO orderDto) {
        String idempotencyKey = UlidCreator.getMonotonicUlid().toString();
        OrderDTO order = orderService.createOrder(orderDto, idempotencyKey);
        System.out.println(6);
        //order.setIdempotencyKey(UlidCreator.getMonotonicUlid().toString());

        // Step 1: ask Inventory to reserve (and pick warehouses)
//        ReserveInventory reserveCmd = new OrderCommand(
//                UUID.randomUUID().toString(),
//                order.getIdempotencyKey(),
//                order.getId(),
//                dto.items(),
//                toAddress(dto),
//                Map.of("command", "ReserveInventory")
//        );
//        kafka.send(Topics.INVENTORY, order.getId().toString(), reserveCmd);
//        // Also publish an order fact if you want an audit stream:
//        publishOrderFact(order.getId(), OrderSagaEventType.ORDER_PLACED, null, Map.of());

        return order;
    }
}
//order.setIdempotencyKey(UlidCreator.getMonotonicUlid().toString());
