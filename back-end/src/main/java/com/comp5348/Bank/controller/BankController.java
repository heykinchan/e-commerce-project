package com.comp5348.Bank.controller;

import com.comp5348.Bank.model.TransactionData;
import com.comp5348.Bank.repository.BankRepository;
import com.comp5348.Bank.service.BankService;
import com.comp5348.dto.TransactionDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private final BankService bankService;
    private final BankRepository bankRepository;
    private final MessageChannel bankToStoreOut;

    @Autowired
    public BankController(BankService bankService, BankRepository bankRepository, @Qualifier("bankToStoreOut") MessageChannel bankToStoreOut) {
        this.bankService = bankService;
        this.bankRepository = bankRepository;
        this.bankToStoreOut = bankToStoreOut;
    }

    // Get all the transactions in the database
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionData>> getAllTransactions(){

        List<TransactionData> transactionDataList = bankRepository.findAll();

        transactionDataList.forEach(transaction -> System.out.println(transaction.toString()));

        return ResponseEntity.ok(transactionDataList);
    }

    // Transaction request is approved, send back the status to Store
    @PutMapping("/transaction/approve/{id}")
    public ResponseEntity<TransactionData> approveTransaction(@PathVariable Long id) throws JsonProcessingException {

        TransactionData result = bankService.updateTransactionDataByID(id,"successful",LocalDateTime.now(),null);

        if (result != null) {
            // Notify Store
            bankService.notifyStore(result,bankToStoreOut);
            // Return the updated transaction in the response
            return ResponseEntity.ok(result);
        } else {
            // If the transaction with the given id is not found
            return ResponseEntity.notFound().build();
        }
    }

    // Transaction request is declined, send back the status to Store
    @PutMapping("/transaction/decline/{id}")
    public ResponseEntity<TransactionData> declineTransaction(@PathVariable Long id) throws JsonProcessingException {

        TransactionData result = bankService.updateTransactionDataByID(id,"declined",null,null);

        if (result != null) {
            // Notify Store
            bankService.notifyStore(result,bankToStoreOut);
            // Return the updated transaction in the response
            return ResponseEntity.ok(result);
        } else {
            // If the transaction with the given id is not found
            return ResponseEntity.notFound().build();
        }
    }

    // Transaction request is refunded, send back the status to Store
    @PutMapping("/transaction/refund/{orderId}")
    public ResponseEntity<TransactionData> refundTransaction(@PathVariable Long orderId) throws JsonProcessingException {

        TransactionData result = bankService.updateTransactionDataByOrderID(orderId,"refunded",null,LocalDateTime.now());

        if (result != null) {
            // Notify the store
            bankService.notifyStore(result,bankToStoreOut);
            // Return the updated transaction in the response
            return ResponseEntity.ok(result);
        } else {
            // If the transaction with the given id is not found
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/clearDB")
    public void clearBankDB(){
        bankRepository.deleteAll();
    }

}
