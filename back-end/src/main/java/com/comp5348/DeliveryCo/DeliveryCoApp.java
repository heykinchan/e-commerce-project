package com.comp5348.DeliveryCo;

import com.comp5348.DeliveryCo.model.DeliveryData;
import com.comp5348.DeliveryCo.service.DeliveryCoService;
import com.comp5348.dto.DeliveryDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.List;

@PropertySource("classpath:application-deliveryco.properties")
@SpringBootApplication(scanBasePackages = "com.comp5348.DeliveryCo")
@EnableScheduling
public class DeliveryCoApp {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryCoApp.class, args);
    }

    // Bean definition for JdbcChannelMessageStore
    // Stores and retrieves messages from the database
    @Bean
    JdbcChannelMessageStore deliveryMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("DELIVERYDATA_");
        return messageStore;
    }

    @Bean
    JdbcChannelMessageStore deliveryToStoreMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("DELIVERYTOSTOREDATA_");
        return messageStore;
    }

    @Bean
    MessageChannel deliveryToStoreOut(JdbcChannelMessageStore deliveryToStoreMessageStore) {
        return MessageChannels.queue(deliveryToStoreMessageStore, "deliverytostore-queue").getObject();
    }

    // Bean definition for IntegrationFlow
    // Handles the inbound messages from the message store
    @Bean
    IntegrationFlow inboundSqlMessagesFlow(JdbcChannelMessageStore deliveryMessageStore, DeliveryCoService deliveryCoService) {
        return IntegrationFlow
                .fromSupplier(() -> deliveryMessageStore.pollMessageFromGroup("delivery-queue"),
                        e -> e.poller(p -> p.fixedDelay(1000, 1000)))
                .handle(message -> {
                    String payload = (String) message.getPayload();

                    try {
                        // Deserialize the JSON array into a list of TradeDataDTO
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        List<DeliveryDataDTO> deliveryDataList = mapper.readValue(payload, new TypeReference<List<DeliveryDataDTO>>() {});

                        // Save each TradeDataDTO using the service
                        deliveryDataList.forEach(deliveryCoService::saveDeliveryData);
                        for(DeliveryDataDTO deliveryDataDTO : deliveryDataList) {
                            System.out.println("Status: " + deliveryDataDTO.getStatus());
                            System.out.println("Order ID: " + deliveryDataDTO.getOrderID());
                            System.out.println("Received Date: " + deliveryDataDTO.getReceiveDate());
                            System.out.println("Pick up Date: " + deliveryDataDTO.getPickUpDate());
                            System.out.println("Sent Date: " + deliveryDataDTO.getSentDate());
                            System.out.println("Finish Date: " + deliveryDataDTO.getFinishDate());
                            System.out.println();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .get();
    }

    // New scheduled task that runs every 5 seconds
    @Bean
    public ScheduledTask scheduledTask(DeliveryCoService deliveryCoService, MessageChannel deliveryToStoreOut) {
        return new ScheduledTask(deliveryCoService, deliveryToStoreOut);
    }

    public static class ScheduledTask {

        private final DeliveryCoService deliveryCoService;
        private final MessageChannel deliveryToStoreOut;

        public ScheduledTask(DeliveryCoService deliveryCoService, MessageChannel deliveryToStoreOut) {
            this.deliveryCoService = deliveryCoService;
            this.deliveryToStoreOut = deliveryToStoreOut;
        }

        // This method will run every 5 seconds
        @Scheduled(fixedRate = 5000)
        public void randomProcessDelivery() {
            List<DeliveryData> deliveryDataList = deliveryCoService.getAllDelivery();
            deliveryDataList.forEach(deliveryData -> {
                // Skip for status "completed","failed","toCancel","cancelled"
                if(deliveryData.getStatus().equals("completed") || deliveryData.getStatus().equals("failed") || deliveryData.getStatus().equals("toCancel") || deliveryData.getStatus().equals("cancelled")) {
                    return;
                } else {
                    try {
                        deliveryCoService.randomProceedDelivery(deliveryData, 0.05, deliveryToStoreOut);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
