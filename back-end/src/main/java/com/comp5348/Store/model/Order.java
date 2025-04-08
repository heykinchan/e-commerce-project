package com.comp5348.Store.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String customerUsername;
    private double totalAmount;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer warehouseId;
    private Integer productId;
    private Integer quantity;

    //future use
    private String transactionStatus;
    private String deliveryStatus;
    // Getters and Setters
}
