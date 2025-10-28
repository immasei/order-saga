package com.example.store.controller;

import com.example.store.dto.inventory.CreateProductDTO;
import com.example.store.dto.inventory.ProductDTO;
import com.example.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Create a new product
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody @Valid CreateProductDTO productDto) {
        ProductDTO product = productService.createProduct(productDto);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductDTO>> createProducts(@RequestBody @Valid List<CreateProductDTO> productDtos) {
        List<ProductDTO> products = productService.createProductsInBatch(productDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(products);
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Get product by product code
    @GetMapping("/{productCode}")
    public ResponseEntity<ProductDTO> getProductByCode(@PathVariable String productCode) {
        ProductDTO product = productService.getProductByCode(productCode);
        return ResponseEntity.ok(product);
    }


    // Get a product by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
//        Optional<Product> product = productRepository.findById(id);
//        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }

    // Update a product by ID
//    @PutMapping("/{id}")
//    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody Product product) {
//        if (productRepository.existsById(id)) {
//            product.setId(id);  // Ensure the product ID stays the same
//            Product updatedProduct = productRepository.save(product);
//            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    // Delete a product by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
//        if (productRepository.existsById(id)) {
//            productRepository.deleteById(id);
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
}
