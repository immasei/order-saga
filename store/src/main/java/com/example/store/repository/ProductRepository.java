package com.example.store.repository;

import com.example.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    // TODO: Implement the below methods
    // List<Product> findByName(String name);
    // List<Product> findByID(String name);
}
