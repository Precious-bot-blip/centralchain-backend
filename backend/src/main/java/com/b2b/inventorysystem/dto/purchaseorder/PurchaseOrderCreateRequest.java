package com.b2b.inventorysystem.dto.purchaseorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCreateRequest {

    @NotNull(message = "Item ID is required")
    private Integer itemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Created-by user ID is required")
    private Integer createdByUserId;

    @NotNull(message = "Supplier ID is required")
    private Integer assignedSupplierId;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;


}