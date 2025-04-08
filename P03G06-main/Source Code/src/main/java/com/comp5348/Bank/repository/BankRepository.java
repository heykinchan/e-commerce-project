package com.comp5348.Bank.repository;

import com.comp5348.Bank.model.TransactionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<TransactionData, Long> {
    Optional<TransactionData> findByOrderID(Long orderID);
}
