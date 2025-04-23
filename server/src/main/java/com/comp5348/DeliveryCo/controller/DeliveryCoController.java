package com.comp5348.DeliveryCo.controller;

import com.comp5348.Bank.model.TransactionData;
import com.comp5348.DeliveryCo.model.DeliveryData;
import com.comp5348.DeliveryCo.repository.DeliveryCoRepository;
import com.comp5348.DeliveryCo.service.DeliveryCoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.DeleteExchange;

import java.util.List;

@RestController
@RequestMapping("/api/deliveryco")
public class DeliveryCoController {

    private final DeliveryCoService deliveryCoService;
    private final DeliveryCoRepository deliveryCoRepository;
    private final MessageChannel deliveryToStoreOut;

    @Autowired
    public DeliveryCoController(DeliveryCoService deliveryCoService, DeliveryCoRepository deliveryCoRepository, @Qualifier("deliveryToStoreOut") MessageChannel deliveryToStoreOut) {
        this.deliveryCoService = deliveryCoService;
        this.deliveryCoRepository = deliveryCoRepository;
        this.deliveryToStoreOut = deliveryToStoreOut;
    }

    @GetMapping("/delivery")
    public ResponseEntity<List<DeliveryData>> getAllDelivery(){

        List<DeliveryData> deliveryDataList = deliveryCoRepository.findAll();
        deliveryDataList.forEach(deliveryData -> System.out.println(deliveryData.toString()));
        return ResponseEntity.ok(deliveryDataList);
    }

    @PutMapping("delivery/receive/{id}")
    public ResponseEntity<DeliveryData> receiveDelivery(@PathVariable Long id) throws JsonProcessingException {

        DeliveryData result = deliveryCoService.updateDeliveryDataByID(id, "received");
        if(result != null){
            deliveryCoService.notifyStore(result,deliveryToStoreOut);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("delivery/collected/{id}")
    public ResponseEntity<DeliveryData> pickUpDelivery(@PathVariable Long id) throws JsonProcessingException {

        DeliveryData result = deliveryCoService.updateDeliveryDataByID(id,"collected");
        if(result != null){
            deliveryCoService.notifyStore(result,deliveryToStoreOut);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("delivery/delivering/{id}")
    public ResponseEntity<DeliveryData> deliveringDelivery(@PathVariable Long id) throws JsonProcessingException {

        DeliveryData result = deliveryCoService.updateDeliveryDataByID(id,"delivering");
        if(result != null){
            deliveryCoService.notifyStore(result,deliveryToStoreOut);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("delivery/delivered/{id}")
    public ResponseEntity<DeliveryData> completeDelivery(@PathVariable Long id) throws JsonProcessingException {

        DeliveryData result = deliveryCoService.updateDeliveryDataByID(id,"completed");
        if(result != null){
            deliveryCoService.notifyStore(result,deliveryToStoreOut);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("delivery/fail/{id}")
    public ResponseEntity<DeliveryData> failDelivery(@PathVariable Long id) throws JsonProcessingException {

        DeliveryData result = deliveryCoService.updateDeliveryDataByID(id,"failed");
        if(result != null){
            deliveryCoService.notifyStore(result,deliveryToStoreOut);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("delivery/cancel/{id}")
    public ResponseEntity<DeliveryData> cancelDelivery(@PathVariable Long id) throws JsonProcessingException {

        DeliveryData result = deliveryCoService.updateDeliveryDataByID(id,"cancelled");
        if(result != null){
            deliveryCoService.notifyStore(result,deliveryToStoreOut);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/clearDB")
    public void clearDeliveryCoDB(){
        deliveryCoRepository.deleteAll();
    }
}
