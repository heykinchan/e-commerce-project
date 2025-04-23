package com.comp5348.DeliveryCo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
public class DeliveryData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String status; // status: "pending","received", "collected","delivering","completed","failed","toCancel","cancelled"
    @Column(unique = true,columnDefinition = "BIGINT")
    private Long orderID;
    @Setter
    private LocalDateTime receiveDate;
    @Setter
    private LocalDateTime pickUpDate;
    @Setter
    private LocalDateTime sentDate;
    @Setter
    private LocalDateTime finishDate;

    public DeliveryData(String status, LocalDateTime receiveDate, LocalDateTime pickUpDate, LocalDateTime sentDate, LocalDateTime finishDate, Long orderID) {
        this.status = status;
        this.orderID = orderID;
        this.receiveDate = receiveDate;
        this.pickUpDate = pickUpDate;
        this.sentDate = sentDate;
        this.finishDate = finishDate;
    }

    public DeliveryData(){}

}
