package com.example.store.repository;

import com.example.store.model.Product;
import com.example.store.model.Stock;
import com.example.store.model.Warehouse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findAllByWarehouse(Warehouse warehouse);
    List<Stock> findAllByProduct(Product product);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stock> findByProductAndWarehouse(Product product, Warehouse warehouse);

    List<Stock> findAllByProductInAndWarehouseIn(Collection<Product> products, Collection<Warehouse> warehouses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from Stock s
        join fetch s.product p
        join fetch s.warehouse w
        where p.productCode in :productCodes
    """)
    List<Stock> findAllByProductCodesForUpdate(@Param("productCodes") Collection<String> productCodes);
}