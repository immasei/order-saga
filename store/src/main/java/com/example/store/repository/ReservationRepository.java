package com.example.store.repository;

import com.example.store.exception.ResourceNotFoundException;
import com.example.store.model.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findByOrderNumber(String orderNumber);

    default Reservation findByOrderNumberOrThrow(String orderNumber) {
        return findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for order: " + orderNumber));
    }

    // Repository: lock-by-orderNumber
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.orderNumber = :orderNumber")
    Optional<Reservation> findByOrderNumberForUpdate(String orderNumber);

}


