package com.centralchain.app.models;

public class InventoryItem {
    public long itemId;
    public String sku;
    public String itemName;
    public String warehouseLocation;
    public int quantityInStock;
    public int minSafetyThreshold;
    public double unitPrice;
    public String updatedAt;

    public boolean isLowStock() {
        return quantityInStock <= minSafetyThreshold;
    }

    // text for spinner
    @Override
    public String toString() {
        return itemName + " (" + quantityInStock + " in stock)";
    }
}
