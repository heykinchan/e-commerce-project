package com.comp5348.DeliveryCo.service;

import com.comp5348.Bank.model.TransactionData;
import com.comp5348.DeliveryCo.model.DeliveryData;
import com.comp5348.DeliveryCo.repository.DeliveryCoRepository;
import com.comp5348.dto.DeliveryDataDTO;
import com.comp5348.dto.TransactionDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class DeliveryCoService {

    private final DeliveryCoRepository deliveryCoRepository;
    private final ObjectMapper mapper;

    @Autowired
    public DeliveryCoService(DeliveryCoRepository deliveryCoRepository) {
        this.deliveryCoRepository = deliveryCoRepository;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public void saveDeliveryData(DeliveryDataDTO deliveryDataDTO) {
        // Check if the delivery exists
        Optional<DeliveryData> optionalDeliveryData = deliveryCoRepository.findByOrderID(deliveryDataDTO.getOrderID());
        if (optionalDeliveryData.isPresent()) {
            DeliveryData deliveryData = optionalDeliveryData.get();
            if(deliveryDataDTO.getStatus().equals("toCancel")){
                deliveryData.setStatus("toCancel");
                deliveryCoRepository.save(deliveryData);
                System.out.println("Receive a request to cancel an delivery for order " + deliveryDataDTO.getOrderID());
            } else {
                System.out.println("The order " +  + deliveryDataDTO.getOrderID() +
                        " has already got an delivery " + deliveryData.getId() +
                        " of status: " + deliveryData.getStatus());
            }
        } else{
            // Convert DTO to entity
            DeliveryData deliveryData = new DeliveryData(
                    deliveryDataDTO.getStatus(),
                    deliveryDataDTO.getReceiveDate(),
                    deliveryDataDTO.getPickUpDate(),
                    deliveryDataDTO.getSentDate(),
                    deliveryDataDTO.getFinishDate(),
                    deliveryDataDTO.getOrderID());
            // Save entity to the database
            deliveryCoRepository.save(deliveryData);
        }
    }

    private DeliveryData updateDeliveryData(Optional<DeliveryData> optionalDeliveryData, String status) {
        if (optionalDeliveryData.isPresent()) {
            // Get the existing transaction
            DeliveryData existingDelivery = optionalDeliveryData.get();
            existingDelivery.setStatus(status);
            if (status.equals("received")) {
                existingDelivery.setReceiveDate(LocalDateTime.now());
            } else if (status.equals("collected")) {
                existingDelivery.setPickUpDate(LocalDateTime.now());
            } else if (status.equals("delivering")) {
                existingDelivery.setSentDate(LocalDateTime.now());
            } else if (status.equals("completed") || status.equals("failed") || status.equals("cancelled")) {
                existingDelivery.setFinishDate(LocalDateTime.now());
            }
            // Save the updated transaction back to the database
            deliveryCoRepository.save(existingDelivery);
            return existingDelivery;
        }
        return null;
    }

    @Transactional
    public DeliveryData updateDeliveryDataByID(Long id, String status) {
        Optional<DeliveryData> optionalDeliveryData = deliveryCoRepository.findById(id);
        return updateDeliveryData(optionalDeliveryData, status);
    }

    @Transactional
    public DeliveryData updateDeliveryDataByOrderID(Long orderID, String status){
        Optional<DeliveryData> optionalDeliveryData = deliveryCoRepository.findByOrderID(orderID);
        return updateDeliveryData(optionalDeliveryData,status);
    }

    @Transactional
    public List<DeliveryDataDTO> getDeliveryDataList(String payload) throws JsonProcessingException {
        return mapper.readValue(payload, new TypeReference<List<DeliveryDataDTO>>() {});
    }

    @Transactional
    public void notifyStore(DeliveryData deliveryData, MessageChannel deliveryToStoreOut) throws JsonProcessingException{
        DeliveryDataDTO deliveryDataDTO = new DeliveryDataDTO(deliveryData);
        // Serialize the DTOs to JSON
        String deliveryJson = mapper.writeValueAsString(deliveryDataDTO);
        // Convert into a single message
        String msg = String.format("[%s]", deliveryJson);
        // Send the JSON message
        deliveryToStoreOut.send(MessageBuilder.withPayload(msg).build());
    }

    @Transactional
    public List<DeliveryData> getAllDelivery(){
        return deliveryCoRepository.findAll();
    }

    @Transactional
    public void randomProceedDelivery(DeliveryData delivery, double failurePercentage, MessageChannel deliveryToStoreOut) throws JsonProcessingException {
        // Random number generator
        Random random = new Random();
        // status: "pending","received", "collected","delivering","completed","failed","toCancel","cancelled"
        // Check the current status of the delivery and proceed based on the status
        String status = delivery.getStatus();
        switch (status) {
            case "pending":
                if (shouldProceed(failurePercentage, random)) {
                    status = "received";
                } else {
                    status = "failed";
                }
                break;

            case "received":
                if (shouldProceed(failurePercentage, random)) {
                    status = "collected";
                } else {
                    status = "failed";
                }
                break;

            case "collected":
                if (shouldProceed(failurePercentage, random)) {
                    status = "delivering";
                } else {
                    status = "failed";
                }
                break;

            case "delivering":
                if (shouldProceed(failurePercentage, random)) {
                    status = "completed";
                } else {
                    status = "failed";
                }
                break;

            default:
                // No action if the status is not one of the processable ones
                break;
        }
        // Save delivery status update (assuming there's a save method in your service)
        this.notifyStore(updateDeliveryDataByID(delivery.getId(), status), deliveryToStoreOut);;
    }

    private boolean shouldProceed(double failurePercentage, Random random) {
        return random.nextDouble() >= failurePercentage;
    }
}
