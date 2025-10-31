package com.example.store.service;

import com.example.store.dto.inventory.AssignStockDTO;
import com.example.store.dto.inventory.CreateWarehouseDTO;
import com.example.store.dto.inventory.InventoryAllocationDTO;
import com.example.store.dto.inventory.ReserveItemDTO;
import com.example.store.dto.inventory.StockDTO;
import com.example.store.dto.inventory.WarehouseDTO;
import com.example.store.enums.ReservationStatus;
import com.example.store.exception.InsufficientStockException;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.kafka.command.ReserveInventory;
import com.example.store.model.*;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.StockRepository;
import com.example.store.repository.WarehouseRepository;
import com.example.store.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final InventoryReservationRepository reservationRepository;
    private final WarehouseStockManager manager;
    private final ModelMapper modelMapper;

    // === warehouses
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

    // === Stocks
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

    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByWarehouseCode(String warehouseCode) {
        Warehouse warehouse = warehouseRepository.findByWarehouseCodeOrThrow(warehouseCode);

        return stockRepository.findAllByWarehouse(warehouse)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByProductCode(String productCode) {
        Product product = productRepository.findByProductCodeOrThrow(productCode);

        return stockRepository.findAllByProduct(product)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // === Reservations
    @Transactional(
        propagation = Propagation.REQUIRED,
        noRollbackFor = InsufficientStockException.class
    ) // no rollback for business failure
    public InventoryAllocationDTO reserveInventory(ReserveInventory cmd) {
        final String orderNumber = cmd.orderNumber();
        final String idempotencyKey = cmd.idempotencyKey();

        // 0. Merge duplicate request items (already validated)
        Map<String, Integer> required = aggregateReserveItems(cmd.items());

        // 1. Lock-or-create reservation row
        InventoryReservation res = reservationRepository.findByOrderNumberForUpdate(orderNumber)
            .orElseGet(() -> {
                InventoryReservation r = new InventoryReservation();
                r.setOrderNumber(orderNumber);
                r.setIdempotencyKey(idempotencyKey);
                return reservationRepository.save(r);
            });

        // 2. Idempotency semantics (no retry)
        if (!Objects.equals(res.getIdempotencyKey(), idempotencyKey)) {
            throw new IllegalStateException("[Reserve Inventory] Idempotency conflict for order " + orderNumber);
        }

        // 3. Short-circuit terminal states (same key)
        if (res.isTerminal()) {
            log.debug("Inventory already processed for order={} status={} - returning cached plan", orderNumber, res.getStatus());
            return toAllocationDto(res);
        }

        // 4. Claim work
        res.setStatus(ReservationStatus.IN_PROGRESS);
        reservationRepository.save(res);

        // 5. Check product code exists
        productRepository.findAllByProductCodeInOrThrow(required.keySet());

        // 6. Lock stock rows for all requested products
        List<Stock> stocks = stockRepository.findAllByProductCodesForUpdate(required.keySet());
        Map<String, List<Stock>> stocksByProduct = stocks.stream()
                .collect(Collectors.groupingBy(s -> s.getProduct().getProductCode()));

        // 7. Check availability
        List<InsufficientStockException.MissingItem> missing = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            String productCode = entry.getKey();
            int needed = entry.getValue();

            int available = stocksByProduct.getOrDefault(productCode, List.of())
                .stream()
                .mapToInt(stock -> stock.getOnHand() - stock.getReserved())
                .sum();

            if (available < needed) {
                missing.add(new InsufficientStockException.MissingItem(productCode, needed, available));
            }
        }

        if (!missing.isEmpty()) {
            // mark reservation failed (idempotent result)
            res.setStatus(ReservationStatus.FAILED);
            res.setFailureReason("Insufficient stock for one or more items");
            reservationRepository.save(res);
            throw new InsufficientStockException("Not enough stock to fulfill order " + orderNumber, missing);
        }

        // 8. Compute allocation plan (your existing logic)
        Map<Stock, Integer> availableByStock = new HashMap<>();
        for (Stock stock : stocks) {
            availableByStock.put(stock, stock.getOnHand() - stock.getReserved());
        }

        AllocationComputation computation = trySingleWarehouse(required, stocksByProduct, availableByStock)
                .orElseGet(() -> allocateAcrossWarehouses(required, stocksByProduct, availableByStock));

        computation.increments().forEach((stock, increment) -> {
            int newReserved = stock.getReserved() + increment;
            stock.setReserved(newReserved);
        });

        // 10) Persist final reservation snapshot
        res.setStatus(ReservationStatus.RESERVED);
        res.getItems().clear();

        for (Map.Entry<Warehouse, Map<Product, Integer>> entry : computation.plan().entrySet()) {
            Warehouse warehouse = entry.getKey();
            for (Map.Entry<Product, Integer> itemEntry : entry.getValue().entrySet()) {
                InventoryReservationItem item = new InventoryReservationItem();
                item.setWarehouse(warehouse);
                item.setProduct(itemEntry.getKey());
                item.setQuantity(itemEntry.getValue());
                res.addItem(item);
            }
        }

        InventoryReservation saved = reservationRepository.save(res);
        log.info(
            "Reserved inventory for order={} warehouses={}",
            orderNumber,
            saved.getItems().stream()
                .map(i -> i.getWarehouse().getWarehouseCode())
                .distinct()
                .toList()
        );

        return toAllocationDto(saved);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public InventoryAllocationDTO releaseReservation(String orderNumber, String reason) {
        InventoryReservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for order " + orderNumber));

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return toAllocationDto(reservation);
        }

        if (reservation.getStatus() == ReservationStatus.COMMITTED) {
            throw new IllegalStateException("Cannot release inventory that has already been committed for order " + orderNumber);
        }

        Map<Stock, Integer> adjustments = new LinkedHashMap<>();
        for (InventoryReservationItem item : reservation.getItems()) {
            Stock stock = stockRepository.findByProductAndWarehouse(item.getProduct(), item.getWarehouse())
                    .orElseThrow(() -> new IllegalStateException("Stock row missing for warehouse="
                            + item.getWarehouse().getWarehouseCode() + " product="
                            + item.getProduct().getProductCode()));
            adjustments.merge(stock, item.getQuantity(), Integer::sum);
        }

        adjustments.forEach((stock, quantity) -> {
            if (stock.getReserved() < quantity) {
                throw new IllegalStateException("Invariant violated: reserved stock would become negative");
            }
            stock.setReserved(stock.getReserved() - quantity);
        });

        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setFailureReason(reason);

        return toAllocationDto(reservation);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public InventoryAllocationDTO commitReservation(String orderNumber) {
        InventoryReservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for order " + orderNumber));

        if (reservation.getStatus() == ReservationStatus.COMMITTED) {
            return toAllocationDto(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Reservation not in RESERVED status for commit, order=" + orderNumber);
        }

        Map<Stock, Integer> adjustments = new LinkedHashMap<>();
        for (InventoryReservationItem item : reservation.getItems()) {
            Stock stock = stockRepository.findByProductAndWarehouse(item.getProduct(), item.getWarehouse())
                    .orElseThrow(() -> new IllegalStateException("Stock row missing for warehouse="
                            + item.getWarehouse().getWarehouseCode() + " product="
                            + item.getProduct().getProductCode()));
            adjustments.merge(stock, item.getQuantity(), Integer::sum);
        }

        adjustments.forEach((stock, quantity) -> {
            if (stock.getReserved() < quantity || stock.getOnHand() < quantity) {
                throw new IllegalStateException("Invariant violated: not enough stock to commit order " + orderNumber);
            }
            stock.setReserved(stock.getReserved() - quantity);
            stock.setOnHand(stock.getOnHand() - quantity);
        });

        reservation.setStatus(ReservationStatus.COMMITTED);
        reservation.setFailureReason(null);

        return toAllocationDto(reservation);
    }

    @Transactional
    public InventoryAllocationDTO getReservation(String orderNumber) {
        InventoryReservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for order " + orderNumber));
        return toAllocationDto(reservation);
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

    // -- helper
    private Map<String, Integer> aggregateReserveItems(List<ReserveItemDTO> items) {
        // items is guaranteed non-null & non-empty by @NotEmpty on ReserveInventory.items
        // productCode is exactly 30 chars, quantity >= 1 by DTO validation
        return items.stream().collect(
            LinkedHashMap::new,
            (map, i) -> map.merge(i.productCode(), i.quantity(), Math::addExact),
            Map::putAll
        );
    }

    private Optional<AllocationComputation> trySingleWarehouse(Map<String, Integer> required,
                                                               Map<String, List<Stock>> stocksByProduct,
                                                               Map<Stock, Integer> availableByStock) {
        Map<Warehouse, Integer> slackByWarehouse = new HashMap<>();

        Set<Warehouse> warehouses = stocksByProduct.values().stream()
                .flatMap(Collection::stream)
                .map(Stock::getWarehouse)
                .collect(Collectors.toSet());

        Warehouse bestWarehouse = null;
        int bestSlack = Integer.MIN_VALUE;

        for (Warehouse warehouse : warehouses) {
            int slack = 0;
            boolean canFulfill = true;
            for (Map.Entry<String, Integer> entry : required.entrySet()) {
                List<Stock> candidates = stocksByProduct.getOrDefault(entry.getKey(), List.of());
                Stock stock = findStockForWarehouse(candidates, warehouse.getId());
                int available = stock == null ? 0 : availableByStock.getOrDefault(stock, 0);
                if (available < entry.getValue()) {
                    canFulfill = false;
                    break;
                }
                slack += (available - entry.getValue());
            }
            if (canFulfill) {
                slackByWarehouse.put(warehouse, slack);
                if (slack > bestSlack) {
                    bestWarehouse = warehouse;
                    bestSlack = slack;
                }
            }
        }

        if (bestWarehouse == null) {
            return Optional.empty();
        }

        Map<Warehouse, Map<Product, Integer>> plan = new LinkedHashMap<>();
        Map<Product, Integer> productPlan = new LinkedHashMap<>();
        Map<Stock, Integer> increments = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            List<Stock> candidates = stocksByProduct.getOrDefault(entry.getKey(), List.of());
            Stock stock = findStockForWarehouse(candidates, bestWarehouse.getId());
            if (stock == null) {
                throw new IllegalStateException("Unexpected state: missing stock for warehouse=" + bestWarehouse.getWarehouseCode());
            }
            int qty = entry.getValue();
            int available = availableByStock.getOrDefault(stock, 0);
            if (available < qty) {
                throw new IllegalStateException("Unexpected insufficient stock during single warehouse allocation");
            }
            availableByStock.put(stock, available - qty);
            increments.merge(stock, qty, Integer::sum);
            productPlan.put(stock.getProduct(), qty);
        }

        plan.put(bestWarehouse, productPlan);
        return Optional.of(new AllocationComputation(plan, increments));
    }

    private AllocationComputation allocateAcrossWarehouses(Map<String, Integer> required,
                                                           Map<String, List<Stock>> stocksByProduct,
                                                           Map<Stock, Integer> availableByStock) {

        Map<Warehouse, Map<Product, Integer>> plan = new LinkedHashMap<>();
        Map<Stock, Integer> increments = new LinkedHashMap<>();
        LinkedHashSet<UUID> preferredWarehouses = new LinkedHashSet<>();

        List<Map.Entry<String, Integer>> sortedProducts = required.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .toList();

        for (Map.Entry<String, Integer> entry : sortedProducts) {
            String productCode = entry.getKey();
            int remaining = entry.getValue();

            List<Stock> candidates = new ArrayList<>(stocksByProduct.getOrDefault(productCode, List.of()));
            candidates.sort((a, b) -> {
                boolean aPreferred = preferredWarehouses.contains(a.getWarehouse().getId());
                boolean bPreferred = preferredWarehouses.contains(b.getWarehouse().getId());
                if (aPreferred != bPreferred) {
                    return aPreferred ? -1 : 1;
                }
                int availDiff = Integer.compare(
                        availableByStock.getOrDefault(b, 0),
                        availableByStock.getOrDefault(a, 0));
                if (availDiff != 0) {
                    return availDiff;
                }
                return a.getWarehouse().getWarehouseCode()
                        .compareTo(b.getWarehouse().getWarehouseCode());
            });

            for (Stock stock : candidates) {
                int available = availableByStock.getOrDefault(stock, 0);
                if (available <= 0) {
                    continue;
                }
                int toReserve = Math.min(remaining, available);
                if (toReserve <= 0) {
                    continue;
                }

                availableByStock.put(stock, available - toReserve);
                increments.merge(stock, toReserve, Integer::sum);

                Warehouse warehouse = stock.getWarehouse();
                Product product = stock.getProduct();
                plan.computeIfAbsent(warehouse, wh -> new LinkedHashMap<>())
                        .merge(product, toReserve, Integer::sum);
                preferredWarehouses.add(warehouse.getId());

                remaining -= toReserve;
                if (remaining == 0) {
                    break;
                }
            }

            if (remaining > 0) {
                throw new InsufficientStockException(
                        "Not enough stock after allocation attempt for product " + productCode,
                        List.of(new InsufficientStockException.MissingItem(productCode, entry.getValue(), entry.getValue() - remaining))
                );
            }
        }

        return new AllocationComputation(plan, increments);
    }

    private Stock findStockForWarehouse(List<Stock> stocks, UUID warehouseId) {
        for (Stock stock : stocks) {
            if (stock.getWarehouse().getId().equals(warehouseId)) {
                return stock;
            }
        }
        return null;
    }

    private InventoryAllocationDTO toAllocationDto(InventoryReservation reservation) {
        Map<String, List<InventoryReservationItem>> itemsByWarehouse = reservation.getItems().stream()
                .collect(Collectors.groupingBy(
                        item -> item.getWarehouse().getWarehouseCode(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<InventoryAllocationDTO.WarehouseAllocationDTO> allocations = itemsByWarehouse.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new InventoryAllocationDTO.WarehouseAllocationDTO(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(item -> new InventoryAllocationDTO.ItemAllocationDTO(
                                        item.getProduct().getProductCode(),
                                        item.getQuantity()
                                ))
                                .sorted(Comparator.comparing(InventoryAllocationDTO.ItemAllocationDTO::productCode))
                                .toList()
                ))
                .toList();

        return new InventoryAllocationDTO(
                reservation.getOrderNumber(),
                reservation.getIdempotencyKey(),
                reservation.getStatus(),
                allocations
        );
    }

    private record AllocationComputation(
            Map<Warehouse, Map<Product, Integer>> plan,
            Map<Stock, Integer> increments
    ) {}
}
