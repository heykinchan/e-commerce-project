package com.comp5348.DeliveryCo.repository;

import com.comp5348.Bank.model.TransactionData;
import com.comp5348.DeliveryCo.model.DeliveryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryCoRepository extends JpaRepository<DeliveryData, Long> {
    Optional<DeliveryData> findByOrderID(Long orderID);
}
