package com.example.store.repository;

import com.example.store.model.Product;
import com.example.store.model.Warehouse;
import com.example.store.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    List<Stock> findAllByWarehouse(Warehouse warehouse);
    List<Stock> findAllByProduct(Product product);
    Optional<Stock> findByProductAndWarehouse(Product product, Warehouse warehouse);
    List<Stock> findAllByProductInAndWarehouseIn(Collection<Product> products, Collection<Warehouse> warehouses);
}