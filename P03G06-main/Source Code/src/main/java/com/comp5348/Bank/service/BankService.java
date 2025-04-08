package com.comp5348.Bank.service;

import com.comp5348.Bank.model.TransactionData;
import com.comp5348.Bank.repository.BankRepository;
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

@Service
public class BankService {

    private final BankRepository bankRepository;
    private final ObjectMapper mapper;

    @Autowired
    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public void saveNewTransactionData(TransactionDataDTO bankDataDTO) {
        // Check if it is an existing transaction
        Optional<TransactionData> optionalTransaction = bankRepository.findByOrderID(bankDataDTO.getOrderID());
        // Check if the transaction exist
        if (optionalTransaction.isPresent()) {
            // If it exists, check if it is a refund
            TransactionData existingTransaction = optionalTransaction.get();
            if(bankDataDTO.getStatus().equals("toRefund")){
                existingTransaction.setStatus("toRefund");
                bankRepository.save(existingTransaction);
                System.out.println("Received a refund request for order " + bankDataDTO.getOrderID());
            } else {
                System.out.println("The order " +  + bankDataDTO.getOrderID() +
                        " has already got an transaction " + existingTransaction.getId() +
                        " of status: " + existingTransaction.getStatus() +
                        ", amount: " + existingTransaction.getAmount());
            }
        } else {
            // Convert DTO to entity
            TransactionData bankData = new TransactionData(bankDataDTO.getFromAcct(),bankDataDTO.getToAcct(),bankDataDTO.getAmount(),bankDataDTO.getOrderID(),bankDataDTO.getStatus());
            // Save entity to the database
            bankRepository.save(bankData);
        }
    }

    @Transactional
    public TransactionData updateTransactionDataByID(Long id, String status, LocalDateTime sentDate, LocalDateTime refundDate) {
        // Find the transaction by id
        Optional<TransactionData> optionalTransaction = bankRepository.findById(id);
        return updateTransactionData(status, sentDate, refundDate, optionalTransaction);
    }

    @Transactional
    public TransactionData updateTransactionDataByOrderID(Long orderID,String status, LocalDateTime sentDate, LocalDateTime refundDate) {
        // Find the transaction by id
        Optional<TransactionData> optionalTransaction = bankRepository.findByOrderID(orderID);
        return updateTransactionData(status, sentDate, refundDate, optionalTransaction);
    }

    private TransactionData updateTransactionData(String status, LocalDateTime sentDate, LocalDateTime refundDate, Optional<TransactionData> optionalTransaction) {
        if (optionalTransaction.isPresent()) {
            // Get the existing transaction
            TransactionData existingTransaction = optionalTransaction.get();
            existingTransaction.setStatus(status);
            if(sentDate != null){
                existingTransaction.setSentDate(sentDate);
            }
            if(refundDate != null){
                existingTransaction.setRefundDate(refundDate);
            }
            // Save the updated transaction back to the database
            bankRepository.save(existingTransaction);
            return existingTransaction;
        }
        return null;
    }

    @Transactional
    public List<TransactionDataDTO> getBankDataList(String payload) throws JsonProcessingException {
        return mapper.readValue(payload, new TypeReference<List<TransactionDataDTO>>() {});
    }

    // Function to inform the Store that the transfer is completed via Messaging Queue
    @Transactional
    public void notifyStore(TransactionData transactionData, MessageChannel bankToStoreOut) throws JsonProcessingException {
        TransactionDataDTO transactionDTO = new TransactionDataDTO(transactionData);
        // Serialize the DTOs to JSON
        String transactionJson = mapper.writeValueAsString(transactionDTO);
        // Convert into a single message
        String msg = String.format("[%s]", transactionJson);
        // Send the JSON message
        bankToStoreOut.send(MessageBuilder.withPayload(msg).build());
    }
}
