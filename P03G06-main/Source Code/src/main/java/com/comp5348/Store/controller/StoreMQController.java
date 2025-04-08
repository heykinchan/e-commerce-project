package com.comp5348.Store.controller;

import com.comp5348.Store.service.StoreMQService;
import com.comp5348.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreMQController {

    private final MessageChannel emailOut;
    private final MessageChannel bankOut;
    private final MessageChannel deliveryOut;
    private final StoreMQService storeMQService;

    @Autowired
    public StoreMQController(MessageChannel emailOut, MessageChannel bankOut, MessageChannel deliveryOut, StoreMQService storeMQService) {
        this.emailOut = emailOut;
        this.bankOut = bankOut;
        this.deliveryOut = deliveryOut;
        this.storeMQService = storeMQService;
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<EmailDataDTO> sendEmail(@RequestBody EmailDataDTO emailDataDTO) throws JsonProcessingException {
        storeMQService.sendEmail(emailDataDTO.getFromEmail(),emailDataDTO.getToEmail(),emailDataDTO.getSubject(),emailDataDTO.getBody(),emailOut);
        return ResponseEntity.ok(emailDataDTO);
    }

    @PostMapping("/requestTransfer")
    public ResponseEntity<TransferRequestDTO> requestTransfer(@RequestBody TransferRequestDTO request) throws JsonProcessingException {
        storeMQService.requestTransfer(request.getOrderID(),request.getAmount(),request.getStatus(),bankOut);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/delivery")
    public ResponseEntity<DeliveryRequestDTO> createDelivery(@RequestBody DeliveryRequestDTO requestDTO) throws JsonProcessingException {
        storeMQService.updateDelivery(requestDTO.getOrderID(),requestDTO.getStatus(),deliveryOut);
        return ResponseEntity.ok(requestDTO);
    }

    @DeleteMapping("/order")
    public ResponseEntity<DeliveryRequestDTO> cancelOrder(@RequestBody DeliveryRequestDTO requestDTO) throws JsonProcessingException {
        storeMQService.cancelOrder(requestDTO.getOrderID(),deliveryOut,emailOut,bankOut);
        return ResponseEntity.ok(requestDTO);
    }
}
