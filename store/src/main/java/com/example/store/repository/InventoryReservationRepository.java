package com.example.store.repository;

import com.example.store.enums.ReservationStatus;
import com.example.store.model.InventoryReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    Optional<InventoryReservation> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumberAndStatus(String orderNumber, ReservationStatus status);

    // Repository: lock-by-orderNumber
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from InventoryReservation r where r.orderNumber = :orderNumber")
    Optional<InventoryReservation> findByOrderNumberForUpdate(String orderNumber);


}


