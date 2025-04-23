package com.comp5348.dto;

import lombok.Data;

@Data
public class TransferRequestDTO {
    private Long orderID;
    private double amount;
    private String status; // status: pending, successful, declined, toRefund, refunded
}
