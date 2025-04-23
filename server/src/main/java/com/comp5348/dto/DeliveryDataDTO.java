package com.comp5348.dto;

import com.comp5348.DeliveryCo.model.DeliveryData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeliveryDataDTO {

    private Long id;
    private String status; // status: "pending","received", "collected","delivering","completed","failed","toCancel","cancelled"
    private Long orderID;
    private LocalDateTime receiveDate;
    private LocalDateTime pickUpDate;
    private LocalDateTime sentDate;
    private LocalDateTime finishDate;

    public DeliveryDataDTO(String status, LocalDateTime receiveDate, LocalDateTime pickUpDate, LocalDateTime sentDate, LocalDateTime finishDate, Long orderID) {
        this.id = null;
        this.status = status;
        this.receiveDate = receiveDate;
        this.pickUpDate = pickUpDate;
        this.sentDate = sentDate;
        this.finishDate = finishDate;
        this.orderID = orderID;
    }

    public DeliveryDataDTO(String status, LocalDateTime receiveDate, LocalDateTime pickUpDate, LocalDateTime sentDate, LocalDateTime finishDate, Long orderID, Long id) {
        this.id = id;
        this.status = status;
        this.orderID = orderID;
        this.pickUpDate = pickUpDate;
        this.receiveDate = receiveDate;
        this.sentDate = sentDate;
        this.finishDate = finishDate;
    }

    public DeliveryDataDTO(DeliveryData deliveryData) {
        this.id = deliveryData.getId();
        this.status = deliveryData.getStatus();
        this.orderID = deliveryData.getOrderID();
        this.receiveDate = deliveryData.getReceiveDate();
        this.pickUpDate = deliveryData.getPickUpDate();
        this.sentDate = deliveryData.getSentDate();
        this.finishDate = deliveryData.getFinishDate();
    }

    public DeliveryDataDTO(){}
}
