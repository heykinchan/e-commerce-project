package com.comp5348.Store.repository;

import com.comp5348.Store.model.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WarehouseRepository extends JpaRepository<WarehouseStock, Integer> {
    List<WarehouseStock> findByProductId(Integer productId);
}