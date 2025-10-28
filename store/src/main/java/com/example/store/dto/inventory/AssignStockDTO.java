package com.example.store.dto.inventory;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssignStockDTO {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100, message = "Warehouse name is too long")
    private String productCode;

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 20, message = "Warehouse code is too long")
    private String warehouseCode;

    @NotNull(message = "Quantity is required")
    private int quantity;

}
