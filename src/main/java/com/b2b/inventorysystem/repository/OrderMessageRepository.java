package com.b2b.inventorysystem.repository;

import com.b2b.inventorysystem.entity.OrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderMessageRepository extends JpaRepository<OrderMessage, Integer> {

    List<OrderMessage> findByPurchaseOrderPoIdOrderBySentAtAsc(Integer poId);
}