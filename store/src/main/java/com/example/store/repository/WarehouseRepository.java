package com.example.store.repository;

import com.example.store.exception.ResourceNotFoundException;
import com.example.store.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Optional<Warehouse> findByWarehouseCode(String code);
    List<Warehouse> findAllByWarehouseCodeIn(Collection<String> codes);

    default Warehouse findByWarehouseCodeOrThrow(String code) {
        return findByWarehouseCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse not found with code: " + code));
    }

    default List<Warehouse> findAllByWarehouseCodeInOrThrow(Collection<String> codes) {
        List<Warehouse> warehouses = findAllByWarehouseCodeIn(codes);
        if (warehouses.size() != codes.size()) {
            List<String> found = warehouses.stream()
                    .map(Warehouse::getWarehouseCode)
                    .toList();
            List<String> missing = codes.stream()
                    .filter(code -> !found.contains(code))
                    .toList();
            throw new ResourceNotFoundException("Warehouses not found with codes: " + missing);
        }
        return warehouses;
    }

}
