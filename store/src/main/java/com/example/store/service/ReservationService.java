package com.example.store.service;

import com.example.store.config.KafkaTopicProperties;
import com.example.store.dto.inventory.*;
import com.example.store.enums.AggregateType;
import com.example.store.enums.ReservationStatus;
import com.example.store.exception.InsufficientStockException;
import com.example.store.exception.ResourceNotFoundException;
import com.example.store.kafka.command.ReleaseInventory;
import com.example.store.kafka.command.ReserveInventory;
import com.example.store.kafka.event.InventoryOutOfStock;
import com.example.store.kafka.event.InventoryReleased;
import com.example.store.kafka.event.InventoryReserved;
import com.example.store.model.*;
import com.example.store.repository.ProductRepository;
import com.example.store.repository.ReservationRepository;
import com.example.store.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final OutboxService outboxService;
    private final KafkaTopicProperties kafkaProps;

    @Transactional(noRollbackFor = InsufficientStockException.class) // no rollback for business failure
    public InventoryAllocationDTO reserveInventory(ReserveInventory cmd) {
        final String orderNumber = cmd.orderNumber();
        final String idempotencyKey = cmd.idempotencyKey();

        // 0. Merge duplicate request items (already validated)
        Map<String, Integer> required = aggregateReserveItems(cmd.items());

        // 1. Lock-or-create reservation row
        Reservation res = reservationRepository.findByOrderNumberForUpdate(orderNumber)
                .orElseGet(() -> {
                    Reservation r = new Reservation();
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
                ReservationItem item = new ReservationItem();
                item.setWarehouse(warehouse);
                item.setProduct(itemEntry.getKey());
                item.setQuantity(itemEntry.getValue());
                res.addItem(item);
            }
        }

        Reservation saved = reservationRepository.save(res);
        InventoryAllocationDTO allocation = toAllocationDto(saved);

        // mark reserved
        InventoryReserved evt = InventoryReserved.of(allocation);
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
        return allocation;
    }

    @Transactional
    public void onInventoryOutOfStock(
        ReserveInventory cmd, List<InsufficientStockException. MissingItem> missingItems
    ) {
        InventoryOutOfStock evt = InventoryOutOfStock.of(cmd.orderNumber(), missingItems);
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional
    public void onOrphanInventoryRelease(ReleaseInventory cmd) {
        InventoryAllocationDTO empty = new InventoryAllocationDTO(
            cmd.orderNumber(),
            cmd.idempotencyKey(),
            ReservationStatus.RELEASED,
            List.of()
        );
        InventoryReleased evt = InventoryReleased.of(empty, cmd.reason());
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public InventoryAllocationDTO releaseReservation(ReleaseInventory cmd) {
        final String orderNumber = cmd.orderNumber();
        final String reason = cmd.reason();

        Reservation reservation = reservationRepository.findByOrderNumberOrThrow(orderNumber);

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return toAllocationDto(reservation);
        }

        if (reservation.getStatus() == ReservationStatus.COMMITTED) {
            throw new IllegalStateException("Cannot release inventory that has already been committed for order " + orderNumber);
        }

        Map<Stock, Integer> adjustments = new LinkedHashMap<>();
        for (ReservationItem item : reservation.getItems()) {
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

        // mark released
        InventoryAllocationDTO allocation = toAllocationDto(reservation);
        InventoryReleased evt = InventoryReleased.of(allocation, cmd.reason());
        emitEvent(cmd.orderNumber(), evt.getClass(), evt);

        return toAllocationDto(reservation);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public InventoryAllocationDTO commitReservation(String orderNumber) {
        Reservation reservation = reservationRepository.findByOrderNumberOrThrow(orderNumber);

        if (reservation.getStatus() == ReservationStatus.COMMITTED) {
            return toAllocationDto(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Reservation not in RESERVED status for commit, order=" + orderNumber);
        }

        Map<Stock, Integer> adjustments = new LinkedHashMap<>();
        for (ReservationItem item : reservation.getItems()) {
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
        Reservation reservation = reservationRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found for order " + orderNumber));
        return toAllocationDto(reservation);
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

    private InventoryAllocationDTO toAllocationDto(Reservation reservation) {
        Map<String, List<ReservationItem>> itemsByWarehouse = reservation.getItems().stream()
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

    private void emitEvent(String aggregateId, Class<?> type, Object payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregateId(aggregateId);
        outbox.setAggregateType(AggregateType.INVENTORY);
        outbox.setEventType(type.getName());
        outbox.setTopic(kafkaProps.inventoryEvents());
        outboxService.save(outbox, payload);
    }
}
