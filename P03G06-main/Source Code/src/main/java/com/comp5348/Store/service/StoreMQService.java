package com.comp5348.Store.service;

import com.comp5348.Store.repository.OrderRepository;
import com.comp5348.dto.DeliveryDataDTO;
import com.comp5348.dto.EmailDataDTO;
import com.comp5348.dto.TransactionDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreMQService {

    private final ObjectMapper mapper;
    private final OrderRepository orderRepository;

    @Autowired
    public StoreMQService(OrderRepository orderRepository) {
        this.mapper = new ObjectMapper();
        this.orderRepository = orderRepository;
        mapper.registerModule(new JavaTimeModule());
    }

    // Function to send a request to the EmailService via Messaging Queue
    public void sendEmail(String from, String to, String subject, String body, MessageChannel emailOut) throws JsonProcessingException {
        EmailDataDTO email = new EmailDataDTO(from,to,subject,body, LocalDateTime.now());
        // Serialize the DTOs to JSON
        String emailJson = mapper.writeValueAsString(email);
        // Convert into a single message
        String msg = String.format("[%s]", emailJson);
        // Send the JSON message
        emailOut.send(MessageBuilder.withPayload(msg).build());
    }

    // Function to request a transfer/refund to the Bank via Messaging Queue
    public void requestTransfer(Long orderID, double amount, String status,MessageChannel bankOut) throws JsonProcessingException {
        TransactionDataDTO transaction = new TransactionDataDTO("merchantAcct","customerAcct",amount,orderID,status);
        // Serialize the DTOs to JSON
        String transactionJson = mapper.writeValueAsString(transaction);
        // Convert into a single message
        String msg = String.format("[%s]", transactionJson);
        // Send the JSON message
        bankOut.send(MessageBuilder.withPayload(msg).build());
    }

    // Function to update a delivery to the DeliveryCo via Messaging Queue
    public void updateDelivery(Long orderID, String status,MessageChannel deliveryOut) throws JsonProcessingException {
        DeliveryDataDTO delivery = new DeliveryDataDTO(status,null,null,null,null,orderID);
        // Serialize the DTOs to JSON
        String deliveryJson = mapper.writeValueAsString(delivery);
        // Convert into a single message
        String msg = String.format("[%s]", deliveryJson);
        // Send the JSON message
        deliveryOut.send(MessageBuilder.withPayload(msg).build());
    }

    // Receive Message from Bank
    public List<TransactionDataDTO> getBankMQList(String payload) throws JsonProcessingException {
        return mapper.readValue(payload, new TypeReference<List<TransactionDataDTO>>() {});
    }

    // Receive Message from DeliveryCo
    public List<DeliveryDataDTO> getDeliveryMQList(String payload) throws JsonProcessingException {
        return mapper.readValue(payload, new TypeReference<List<DeliveryDataDTO>>() {});
    }

    // Cancel an order
    public void cancelOrder(Long orderID, MessageChannel deliveryOut, MessageChannel emailOut, MessageChannel bankOut) throws JsonProcessingException {
        // Email to confirm the order cancellation
        this.sendEmail("merchant@mail.com","customer@mail.com","Your order is cancelled",
                "Your order (order ID: " + orderID + ") has been cancelled. The delivery will be cancelled and the bank transfer will be refunded shortly.",emailOut);
        // Cancel the delivery
        this.updateDelivery(orderID,"toCancel",deliveryOut);
        // Refund to customer
        this.requestTransfer(orderID,-1,"toRefund",bankOut);
    }

}
