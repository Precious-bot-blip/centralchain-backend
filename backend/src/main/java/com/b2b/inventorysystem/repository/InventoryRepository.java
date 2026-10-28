package com.b2b.inventorysystem.repository;

import com.b2b.inventorysystem.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Inventory> findByWarehouseLocation(String warehouseLocation);

    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock <= i.minSafetyThreshold")
    List<Inventory> findLowStockItems();

    @Query("SELECT i.warehouseLocation, SUM(i.quantityInStock) FROM Inventory i GROUP BY i.warehouseLocation")
    List<Object[]> findStockByWarehouse();

}