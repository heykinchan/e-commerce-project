package com.comp5348.Bank;

import com.comp5348.Bank.service.BankService;
import com.comp5348.dto.TransactionDataDTO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.MessageChannel;

import javax.sql.DataSource;
import java.util.List;

@PropertySource("classpath:application-bank.properties")
@SpringBootApplication(scanBasePackages = "com.comp5348.Bank")
public class BankApp {

    public static void main(String[] args) {
        SpringApplication.run(BankApp.class, args);
    }

    // Bean definition for JdbcChannelMessageStore
    // Stores and retrieves messages from the database
    @Bean
    JdbcChannelMessageStore bankMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("BANKDATA_");
        return messageStore;
    }

    @Bean
    JdbcChannelMessageStore bankToStoreMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("BANKTOSTOREDATA_");
        return messageStore;
    }

    @Bean
    MessageChannel bankToStoreOut(JdbcChannelMessageStore bankToStoreMessageStore) {
        return MessageChannels.queue(bankToStoreMessageStore, "banktostore-queue").getObject();
    }

    // Bean definition for IntegrationFlow
    // Handles the inbound messages from the message store
    @Bean
    IntegrationFlow inboundSqlMessagesFlow(JdbcChannelMessageStore bankMessageStore, BankService bankService) {
        return IntegrationFlow
                .fromSupplier(() -> bankMessageStore.pollMessageFromGroup("bank-queue"),
                        e -> e.poller(p -> p.fixedDelay(1000, 1000)))
                .handle(message -> {
                    String payload = (String) message.getPayload();

                    try {
                        // Deserialize the JSON array into a list of TradeDataDTO
                        List<TransactionDataDTO> bankDataList = bankService.getBankDataList(payload);

                        // Save each TradeDataDTO using the service
                        bankDataList.forEach(bankService::saveNewTransactionData);
                        for(TransactionDataDTO bankfeed : bankDataList) {
                            System.out.println("From Account: " + bankfeed.getFromAcct());
                            System.out.println("To Account: " + bankfeed.getToAcct());
                            System.out.println("Amount: " + bankfeed.getAmount());
                            System.out.println("Status: " + bankfeed.getStatus());
                            System.out.println("Order ID: " + bankfeed.getOrderID());
                            System.out.println();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .get();
    }
}