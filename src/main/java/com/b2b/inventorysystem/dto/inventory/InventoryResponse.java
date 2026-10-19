package com.b2b.inventorysystem.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Integer itemId;
    private String sku;
    private String itemName;
    private String warehouseLocation;
    private Integer quantityInStock;
    private Integer minSafetyThreshold;
    private BigDecimal unitPrice;
    private LocalDateTime updatedAt;
}