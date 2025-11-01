package com.example.store.service;

import com.example.store.dto.inventory.CreateProductDTO;
import com.example.store.dto.inventory.ProductDTO;
import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ProductDTO createProduct(CreateProductDTO productDto) {
        Product product = productRepository.save(toEntity(productDto));
        return toResponse(product);
    }

    @Transactional
    public List<ProductDTO> createProductsInBatch(List<CreateProductDTO> productDtos) {
        if (productDtos == null || productDtos.isEmpty()) return List.of();

        List<Product> products = productDtos.stream()
                .map(this::toEntity)
                .toList();

        // save all or nothing
        List<Product> saved = productRepository.saveAllAndFlush(products);

        return saved.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductByCode(String productCode) {
        Product product = productRepository
                .findByProductCodeOrThrow(productCode);
        return toResponse(product);
    }

    // --- Mapper
    public Product toEntity(CreateProductDTO dto) {
        return modelMapper.map(dto, Product.class);
    }

    public ProductDTO toResponse(Product prd) {
        return modelMapper.map(prd, ProductDTO.class);
    }
}