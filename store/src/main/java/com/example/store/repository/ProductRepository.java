package com.example.store.repository;

import com.example.store.exception.ResourceNotFoundException;
import com.example.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByProductCode(String productCode);
    List<Product> findAllByProductCodeIn(Collection<String> productCodes);

    default Product findByProductCodeOrThrow(String productCode) {
        return findByProductCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with code: " + productCode));
    }

    default List<Product> findAllByProductCodeInOrThrow(Collection<String> productCodes) {
        List<Product> products = findAllByProductCodeIn(productCodes);
        if (products.size() != productCodes.size()) {
            // find missing codes
            List<String> foundCodes = products.stream()
                    .map(Product::getProductCode)
                    .toList();
            List<String> missing = productCodes.stream()
                    .filter(code -> !foundCodes.contains(code))
                    .toList();
            throw new ResourceNotFoundException("Products not found with codes: " + missing);
        }
        return products;
    }
}
