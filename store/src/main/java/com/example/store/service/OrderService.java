package com.example.store.service;

import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.dto.order.CreateOrderItemDTO;
import com.example.store.dto.order.OrderItemDTO;
import com.example.store.enums.UserRole;
import com.example.store.model.*;
import com.example.store.repository.UserRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDTO createOrder(CreateOrderDTO orderDto, String idempotencyKey) {
        // get existing customer or throw
        Customer user = (Customer) userRepository
                .getOrThrow(orderDto.getCustomerId(), UserRole.CUSTOMER);

        Set<String> productCodes = orderDto.getOrderItems().stream()
                .map(CreateOrderItemDTO::getProductCode)
                .collect(Collectors.toSet());

        // get existing products or throw
        List<Product> products = productRepository
                .findAllByProductCodeInOrThrow(productCodes);
        Map<String, Product> productByCode = products.stream()
                .collect(Collectors.toMap(Product::getProductCode, p -> p));

        // build order
        Order order = toEntity(orderDto);
        order.setCustomer(user);
        order.setIdempotencyKey(idempotencyKey);
        order.getOrderItems().clear();
        if (orderDto.getDeliveryAddress() == null)
            order.setDeliveryAddress(user.getAddress());

        // Build order items
        for (CreateOrderItemDTO itemDto : orderDto.getOrderItems()) {

            Product p = productByCode.get(itemDto.getProductCode());

            OrderItem i = new OrderItem();
            i.setProduct(p);
            i.setProductCodeAtPurchase(p.getProductCode());
            i.setProductNameAtPurchase(p.getProductName());
            i.setUnitPrice(p.getPrice());
            i.setQuantity(itemDto.getQuantity());
            i.computeLineTotal();

            order.addOrderItem(i); // maintains both sides
        }

        Order saved = orderRepository.saveAndFlush(order);
        return toResponse(saved);
    }

    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository
                .findByOrderNumberOrThrow(orderNumber);
        return toResponse(order);
    }

    public List<OrderDTO> getOrdersByCustomerId(UUID customerId) {
        return orderRepository
                .findAllByCustomer_IdOrderByPlacedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderDTO> getALlOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Order toEntity(CreateOrderDTO dto) {
        return modelMapper.map(dto, Order.class);
    }

    public OrderItemDTO toResponse(OrderItem i) {
        return modelMapper.map(i, OrderItemDTO.class);
    }

    public OrderDTO toResponse(Order o) {
        OrderDTO dto = modelMapper.map(o, OrderDTO.class);
        List<OrderItemDTO> orderItems = o.getOrderItems().stream()
                .map(this::toResponse)
                .toList();

        dto.setCustomerEmail(o.getCustomer().getEmail());
        dto.setOrderItems(orderItems);
        return dto;
    }

}
