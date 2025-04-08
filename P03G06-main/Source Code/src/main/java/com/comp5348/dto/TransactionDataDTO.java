package com.comp5348.dto;

import com.comp5348.Bank.model.TransactionData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDataDTO {

    private Long id;
    private String fromAcct;
    private String toAcct;
    private double amount;
    private Long orderID;
    private String status;
    private LocalDateTime sentDate;
    private LocalDateTime refundDate;

    public TransactionDataDTO(String fromAcct, String toAcct, double amount, Long orderID, String status) {
        this.id = null;
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

    public TransactionDataDTO(TransactionData transactionData) {
        this.id = transactionData.getId();
        this.fromAcct = transactionData.getFromAcct();
        this.toAcct = transactionData.getToAcct();
        this.amount = transactionData.getAmount();
        this.orderID = transactionData.getOrderID();
        this.status = transactionData.getStatus();
        this.sentDate = transactionData.getSentDate();
        this.refundDate = transactionData.getRefundDate();
    }

    public TransactionDataDTO() {
    }
}
