package com.example.store.repository;

import com.example.store.exception.ResourceNotFoundException;
import com.example.store.model.Order;
import com.example.store.model.User;
import com.example.store.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findAllByCustomer_IdOrderByPlacedAtDesc(UUID customerId);

    default Order findByOrderNumberOrThrow(String orderNumber) {
        return findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with code: " + orderNumber));
    }

}
