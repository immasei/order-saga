package com.example.store.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateWarehouseDTO {

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 20, message = "Warehouse code is too long")
    private String warehouseCode;

    @Size(max = 100, message = "Warehouse name is too long")
    private String warehouseName;

    @NotBlank(message = "Warehouse location is required")
    @Size(max = 100, message = "Warehouse location is too long")
    private String location;

}
