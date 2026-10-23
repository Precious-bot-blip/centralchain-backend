package com.b2b.inventorysystem.repository;

import com.b2b.inventorysystem.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    List<PurchaseOrder> findByStatus(String status);

    List<PurchaseOrder> findByAssignedSupplierUserId(Integer supplierId);

    List<PurchaseOrder> findByAssignedDriverUserId(Integer driverId);

    List<PurchaseOrder> findByCreatedByUserUserId(Integer userId);

    List<PurchaseOrder> findByStatusAndAssignedSupplierUserId(String status, Integer supplierId);

    @Query("SELECT p.poNumber FROM PurchaseOrder p WHERE p.poNumber LIKE :prefix% ORDER BY p.poNumber DESC LIMIT 1")
    Optional<String> findLatestPoNumberWithPrefix(@Param("prefix") String prefix);
}