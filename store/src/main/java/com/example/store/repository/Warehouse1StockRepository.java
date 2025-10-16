package com.example.store.repository;

import com.example.store.model.Warehouse1Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface Warehouse1StockRepository extends JpaRepository<Warehouse1Stock, UUID> {
    Optional<Object> findById(UUID[] uuids);
}
