package com.b2b.inventorysystem.repository;

import com.b2b.inventorysystem.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    Optional<Delivery> findByPurchaseOrderPoId(Integer poId);

    List<Delivery> findByDriverUserId(Integer driverId);

    List<Delivery> findByDeliveryStatus(String status);
}