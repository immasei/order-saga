package com.example.store.service;

import com.example.store.dto.inventory.AssignStockDTO;
import com.example.store.dto.inventory.CreateWarehouseDTO;
import com.example.store.dto.inventory.StockDTO;
import com.example.store.dto.inventory.WarehouseDTO;
import com.example.store.model.Product;
import com.example.store.model.Stock;
import com.example.store.model.Warehouse;
import com.example.store.model.WarehouseStockManager;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.WarehouseRepository;
import com.example.store.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final WarehouseStockManager manager;
    private final ModelMapper modelMapper;

    @Transactional
    public WarehouseDTO createWarehouse(CreateWarehouseDTO warehouseDto) {
        Warehouse warehouse = toEntity(warehouseDto);
        Warehouse saved = warehouseRepository.save(warehouse);

        // create warehouse stock table for this warehouse
        manager.createPartition(saved.getId(), saved.getWarehouseCode());
        return toResponse(saved);
    }

    @Transactional
    public List<WarehouseDTO> createWarehousesInBatch(List<CreateWarehouseDTO> warehouseDtos) {
        if (warehouseDtos == null || warehouseDtos.isEmpty()) return List.of();
        List<Warehouse> warehouses = warehouseDtos.stream()
                .map(this::toEntity)
                .toList();

        // save all or nothing
        List<Warehouse> saved = warehouseRepository.saveAllAndFlush(warehouses);

        // create warehouse stock table for each warehouse
        saved.forEach(wh ->
            manager.createPartition(wh.getId(), wh.getWarehouseCode())
        );

        return saved.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public WarehouseDTO getWarehouseByCode(String productCode) {
        Warehouse warehouse = warehouseRepository
                .findByWarehouseCodeOrThrow(productCode);
        return toResponse(warehouse);
    }

    @Transactional
    public StockDTO assignStock(AssignStockDTO stockDto) {
        // get existing or throw
        Product product = productRepository
                .findByProductCodeOrThrow(stockDto.getProductCode());
        Warehouse warehouse = warehouseRepository
                .findByWarehouseCodeOrThrow(stockDto.getWarehouseCode());

        // find existing or create new stock
        Optional<Stock> stocks = stockRepository
                .findByProductAndWarehouse(product, warehouse);

        Stock stock = stocks.orElseGet(() -> {
            Stock s = new Stock();
            s.setProduct(product);
            s.setWarehouse(warehouse);
            s.setOnHand(0);
            s.setReserved(0);
            return s;
        });

        // set stock quantity
        int newQty = stock.getOnHand() + stockDto.getQuantity();
        if (newQty < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        stock.setOnHand(newQty);

        Stock saved = stockRepository.save(stock);
        return toResponse(saved);
    }

    @Transactional
    public List<StockDTO> assignStocksInBatch(List<AssignStockDTO> stockDtos) {
        if (stockDtos == null || stockDtos.isEmpty()) return List.of();

        Set<String> productCodes = stockDtos.stream()
                .map(AssignStockDTO::getProductCode)
                .collect(Collectors.toSet());
        Set<String> warehouseCodes = stockDtos.stream()
                .map(AssignStockDTO::getWarehouseCode)
                .collect(Collectors.toSet());

        // get existing or throw
        List<Product> products = productRepository
                .findAllByProductCodeInOrThrow(productCodes);
        List<Warehouse> warehouses = warehouseRepository
                .findAllByWarehouseCodeInOrThrow(warehouseCodes);

        // map for product and warehouse
        Map<String, Warehouse> warehouseByCode = warehouses.stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseCode, w -> w));
        Map<String, Product> productByCode = products.stream()
                .collect(Collectors.toMap(Product::getProductCode, p -> p));

        // get existing stocks by product codes OR warehouse codes
        List<Stock> stocks = stockRepository
                .findAllByProductInAndWarehouseIn(products, warehouses);

        // create a map for existing stocks
        record Key(UUID warehouseId, UUID productId) {}
        Map<Key, Stock> stocksById = new HashMap<>();
        for (var s : stocks) {
            stocksById.put(new Key(s.getWarehouse().getId(), s.getProduct().getId()), s);
        }

        List<Stock> toSave = new ArrayList<>();
        for (AssignStockDTO dto : stockDtos) {

            Product product = productByCode.get(dto.getProductCode());
            Warehouse warehouse = warehouseByCode.get(dto.getWarehouseCode());

            // find existing or create new stock
            Key k = new Key(warehouse.getId(), product.getId());
            Stock stock = stocksById.get(k);
            if (stock == null) {
                stock = new Stock();
                stock.setProduct(product);
                stock.setWarehouse(warehouse);
                stock.setOnHand(0);
                stock.setReserved(0);
                // stock dto can contain duplicate request by (warehouse code, product code)
                // we put into map so subsequent merges on same pair reuse it
                stocksById.put(k, stock);
            }

            int newQty = stock.getOnHand() + dto.getQuantity();
            if (newQty < 0) {
                throw new IllegalArgumentException("Stock quantity will become negative");
            }
            stock.setOnHand(newQty);
            toSave.add(stock);
        }

        List<Stock> saved = stockRepository.saveAllAndFlush(toSave);
        return saved.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<StockDTO> getStocksByWarehouseCode(String warehouseCode) {
        Warehouse warehouse = warehouseRepository.findByWarehouseCodeOrThrow(warehouseCode);

        return stockRepository.findAllByWarehouse(warehouse)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<StockDTO> getStocksByProductCode(String productCode) {
        Product product = productRepository.findByProductCodeOrThrow(productCode);

        return stockRepository.findAllByProduct(product)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Mapper
    public Warehouse toEntity(CreateWarehouseDTO dto) {
        return modelMapper.map(dto, Warehouse.class);
    }

    public WarehouseDTO toResponse(Warehouse wh) {
        return modelMapper.map(wh, WarehouseDTO.class);
    }

    public StockDTO toResponse(Stock s) {
        StockDTO dto = modelMapper.map(s, StockDTO.class);
        dto.setWarehouseCode(s.getWarehouse().getWarehouseCode());
        dto.setProductCode(s.getProduct().getProductCode());
        return dto;
    }
}
