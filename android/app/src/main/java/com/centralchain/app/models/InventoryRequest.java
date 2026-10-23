package com.centralchain.app.models;

// for add and edit item
public class InventoryRequest {
    public String sku;
    public String itemName;
    public String warehouseLocation;
    public Integer quantityInStock;
    public Integer minSafetyThreshold;
    public Double unitPrice;
}
