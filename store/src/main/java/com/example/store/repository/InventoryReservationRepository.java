package com.example.store.repository;

import com.example.store.enums.ReservationStatus;
import com.example.store.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    Optional<InventoryReservation> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumberAndStatus(String orderNumber, ReservationStatus status);
}


