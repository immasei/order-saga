package com.example.store.model;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ALTER TABLE warehouse_stock
 * PARTITION BY LIST (warehouse_id);
 */

@Component
@RequiredArgsConstructor
public class WarehouseStockManager {

    private final JdbcTemplate jdbc;

    public void createPartition(UUID warehouseId, String warehouseCode) {
        String shortCode = sanitize(warehouseCode);
        String tbl = "warehouse_stock_" + shortCode;

        // Create partition table
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS %s
            PARTITION OF warehouse_stock
            FOR VALUES IN ('%s')
        """.formatted(tbl, warehouseId));
    }

    public void dropPartition(UUID warehouseId, String warehouseCode) {
        String shortCode = sanitize(warehouseCode);
        String tbl = "warehouse_stock_" + shortCode;

        jdbc.execute("DROP TABLE IF EXISTS " + tbl + " CASCADE");
    }

    public void renamePartition(String oldCode, String newCode) {
        String oldTbl = "warehouse_stock_" + sanitize(oldCode);
        String newTbl = "warehouse_stock_" + sanitize(newCode);

        jdbc.execute("ALTER TABLE IF EXISTS " + oldTbl + " RENAME TO " + newTbl);
    }

    private String sanitize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

}

