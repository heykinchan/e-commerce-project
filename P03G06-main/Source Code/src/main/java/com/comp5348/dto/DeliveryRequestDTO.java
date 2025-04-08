package com.comp5348.dto;

import lombok.Data;

@Data
public class DeliveryRequestDTO {
    private Long orderID;
    private String status; // status: "pending","received", "collected","delivering","completed","failed","toCancel","cancelled"
}
