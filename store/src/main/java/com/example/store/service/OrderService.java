package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.order.CreateOrderDTO;
import com.example.store.dto.order.OrderDTO;
import com.example.store.dto.order.CreateOrderItemDTO;
import com.example.store.dto.order.OrderItemDTO;
import com.example.store.enums.AggregateType;
import com.example.store.enums.OrderStatus;
import com.example.store.enums.UserRole;
import com.example.store.kafka.event.OrderPlaced;
import com.example.store.model.*;
import com.example.store.repository.OrderItemRepository;
import com.example.store.repository.UserRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.OrderRepository;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ModelMapper modelMapper;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
    private final KafkaTopicProperties kafkaProps;

    // === Entry point: user placed order, outbox OrderPlaced ===
    @Transactional
    public OrderDTO placeOrder(CreateOrderDTO orderDto) {
        String idempotencyKey = UlidCreator.getMonotonicUlid().toString();

        // 1. save order to db
        OrderDTO order = createOrder(orderDto, idempotencyKey);

        // 2. create OrderPlaced event (aka fact)
        OrderPlaced evt = OrderPlaced.of(order, idempotencyKey);

        // 3. save OrderCreated to db, this event will later be
        //    published by kafka/OutboxPublisher
        Outbox outbox = new Outbox();
        outbox.setAggregateId(order.getOrderNumber());
        outbox.setAggregateType(AggregateType.ORDER);
        outbox.setEventType(evt.getClass().getName());
        outbox.setTopic(kafkaProps.ordersEvents());
        outboxService.save(outbox, evt);

        return order;
    }

    @Transactional(propagation = Propagation.REQUIRED)
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

        Map<String, OrderItem> orderItemByProduct = new HashMap<>();
        // build order
        Order order = toEntity(orderDto);
        order.setCustomer(user);
        order.setIdempotencyKey(idempotencyKey);
        System.out.println(order); //ok

        order.getOrderItems().clear();
        if (orderDto.getDeliveryAddress() == null)
            order.setDeliveryAddress(user.getAddress());

        // Build order items
        for (CreateOrderItemDTO itemDto : orderDto.getOrderItems()) {
            String pCode = itemDto.getProductCode();
            Product p = productByCode.get(pCode);

            // merge duplicated items if needed
            OrderItem item = orderItemByProduct.computeIfAbsent(pCode, code -> {
                OrderItem i = new OrderItem();
                i.setProduct(p);
                // snapshot fields at purchase time
                i.setProductCodeAtPurchase(p.getProductCode());
                i.setProductNameAtPurchase(p.getProductName());
                i.setUnitPrice(p.getPrice());
                order.addOrderItem(i); // maintain both sides
                return i;
            });

            item.increaseQuantity(itemDto.getQuantity());
            item.computeLineTotal();
        }

        Order saved = orderRepository.saveAndFlush(order);//throw
        System.out.println(saved.getIdempotencyKey());//dont reach
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

    @Transactional
    public void updateOrderStatus(String orderNumber, OrderStatus newStatus) {
        Order order = orderRepository
                .findByOrderNumberForUpdateOrThrow(orderNumber);
        order.setStatus(newStatus);
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
