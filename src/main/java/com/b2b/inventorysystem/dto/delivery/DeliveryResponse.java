package com.b2b.inventorysystem.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private Integer deliveryId;

    // PO info
    private Integer poId;
    private String poNumber;
    private Integer poQuantity;

    // Item info
    private Integer itemId;
    private String itemSku;
    private String itemName;

    // Driver info
    private Integer driverId;
    private String driverName;

    // Delivery details
    private String deliveryStatus;
    private String signatureUrl;
    private String photoProofUrl;
    private LocalDateTime deliveredAt;
}