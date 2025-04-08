package com.comp5348.Bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Entity
public class TransactionData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAcct;
    private String toAcct;
    private double amount;

    @Column(unique = true, columnDefinition = "BIGINT")
    private Long orderID;
    @Setter
    private String status;
    @Setter
    private LocalDateTime sentDate;
    @Setter
    private LocalDateTime refundDate;

    public TransactionData(String fromAcct, String toAcct, double amount, Long orderID, String status) {
        this.fromAcct = fromAcct;
        this.toAcct = toAcct;
        this.amount = amount;
        this.orderID = orderID;
        if(status == null){
            this.status = "pending";
        } else {
            this.status = status;
        }
        // status: pending, successful, declined, toRefund, refunded
    }

    public TransactionData() {
    }
}