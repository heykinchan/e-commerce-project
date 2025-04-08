package com.comp5348.Store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warehouse_stock")
@Getter @Setter
public class WarehouseStock {
    @Id
    private Integer warehouseId;
    private Integer productId;
    private Integer stockLevel;

    // Getters and Setters
}