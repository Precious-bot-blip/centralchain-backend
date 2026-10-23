package com.b2b.inventorysystem.dto.purchaseorder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {

    private Integer poId;
    private String poNumber;

    // Item info
    private Integer itemId;
    private String itemSku;
    private String itemName;

    private Integer quantity;
    private String status;

    // User info
    private Integer createdByUserId;
    private String createdByUserName;

    private Integer assignedSupplierId;
    private String assignedSupplierName;

    private Integer assignedDriverId;
    private String assignedDriverName;

    private LocalDateTime createdAt;
}