package com.example.store.repository;

import com.example.store.model.ProductPurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;


@Repository
public interface ProductPurchaseHistoryRepository extends JpaRepository<ProductPurchaseHistory, UUID> {
    Optional<ProductPurchaseHistory> findByProductId(UUID productId);

}
