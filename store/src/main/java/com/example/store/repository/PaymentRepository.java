package com.example.store.repository;

import com.example.store.exception.ResourceNotFoundException;
import com.example.store.model.Order;
import com.example.store.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrder(Order order);

    default Payment findByOrderOrThrow(Order order) {
        return findByOrder(order)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Payment not found for order " + order.getOrderNumber()
            ));
    }

}