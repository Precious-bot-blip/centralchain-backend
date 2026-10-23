package com.b2b.inventorysystem.dto.inventory;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "Item name is required")
    @Size(max = 100)
    private String itemName;

    @NotBlank(message = "Warehouse location is required")
    @Size(max = 100)
    private String warehouseLocation;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantityInStock;

    @NotNull(message = "Minimum safety threshold is required")
    @Min(value = 0, message = "Threshold cannot be negative")
    private Integer minSafetyThreshold;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 digits and 2 decimals")
    private BigDecimal unitPrice;
}