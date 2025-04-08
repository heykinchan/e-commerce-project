package com.comp5348.Store;

import com.comp5348.DeliveryCo.model.DeliveryData;
import com.comp5348.Store.model.Order;
import com.comp5348.Store.repository.OrderRepository;
import com.comp5348.Store.service.OrderService;
import com.comp5348.Store.service.SseService;
import com.comp5348.Store.service.StoreMQService;
import com.comp5348.dto.DeliveryDataDTO;
import com.comp5348.dto.TransactionDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.ApplicationRunner;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@PropertySource("classpath:application-store.properties")
@SpringBootApplication(scanBasePackages = "com.comp5348.Store")
public class StoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreApplication.class, args);
    }

    // Bean definition for JdbcChannelMessageStore
    // Stores and retrieves messages from the database
    @Bean
    JdbcChannelMessageStore emailMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("EMAILDATA_");
        return messageStore;
    }

    @Bean
    JdbcChannelMessageStore bankMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("BANKDATA_");
        return messageStore;
    }

    @Bean
    JdbcChannelMessageStore deliveryMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("DELIVERYDATA_");
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
    JdbcChannelMessageStore deliveryToStoreMessageStore(DataSource dataSource) {
        var messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        messageStore.setTablePrefix("DELIVERYTOSTOREDATA_");
        return messageStore;
    }

    // Bean definition for MessageChannel
    // Acts as a queue to store messages before they are processed
    @Bean
    MessageChannel emailOut(JdbcChannelMessageStore emailMessageStore) {
        return MessageChannels.queue(emailMessageStore, "email-queue").getObject();
    }

    @Bean
    MessageChannel bankOut(JdbcChannelMessageStore bankMessageStore) {
        return MessageChannels.queue(bankMessageStore, "bank-queue").getObject();
    }

    @Bean
    MessageChannel deliveryOut(JdbcChannelMessageStore deliveryMessageStore) {
        return MessageChannels.queue(deliveryMessageStore, "delivery-queue").getObject();
    }

    // Bean definition for ApplicationRunner
    // Executes the business logic to produce and send messages
    @Bean
    ApplicationRunner runner(MessageChannel emailOut, MessageChannel bankOut, MessageChannel deliveryOut, StoreMQService storeMQService) {
        return args -> {
            // Business Logic here
            // Jackson ObjectMapper for JSON conversion
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            while (true) {
//                // Generate Email
//                storeMQService.sendEmail("1@mail.com","2@mail.com","hello","testing",emailOut);
//
//                storeMQService.requestTransfer("from1","to1",100.0,1L,null,bankOut);
//
//                storeMQService.createDelivery("received",LocalDateTime.now(),deliveryOut);

                // Define the interval to generate price and send message
                Thread.sleep(10000);
            }
        };
    }

    @Bean
    IntegrationFlow BTSinboundSqlMessagesFlow(JdbcChannelMessageStore bankToStoreMessageStore, StoreMQService storeMQService, MessageChannel emailOut, OrderRepository orderRepository, OrderService orderService, MessageChannel deliveryOut, SseService sseService) {
        return IntegrationFlow
                .fromSupplier(() -> bankToStoreMessageStore.pollMessageFromGroup("banktostore-queue"),
                        e -> e.poller(p -> p.fixedDelay(1000, 1000)))
                .handle(message -> {
                    String payload = (String) message.getPayload();

                    try {
                        // Deserialize the JSON array into a list of TradeDataDTO
                        List<TransactionDataDTO> bankMQList = storeMQService.getBankMQList(payload);

                        for(TransactionDataDTO bankmsg : bankMQList) {
                            System.out.println("Message Received from Bank: ");
                            System.out.println("Payment ID: "+ bankmsg.getId());
                            System.out.println("From Account: " + bankmsg.getFromAcct());
                            System.out.println("To Account: " + bankmsg.getToAcct());
                            System.out.println("Amount: " + bankmsg.getAmount());
                            System.out.println("Status: " + bankmsg.getStatus());
                            System.out.println("Order ID: " + bankmsg.getOrderID());
                            System.out.println();

                            // Update order status in the database
                            Optional<Order> optionalOderData = orderRepository.findById(bankmsg.getOrderID());
                            if(optionalOderData.isPresent()){
                              Order order = optionalOderData.get();
                              order.setTransactionStatus(bankmsg.getStatus());
                              orderRepository.save(order);
                              if(bankmsg.getStatus().equals("successful") && order.getStatus().equals("PENDING")){
                                  orderService.updateWarehouse(order);
                                  storeMQService.updateDelivery(order.getOrderId(),"pending",deliveryOut);
                              }
                            }

                            // Notify the front end to update
                            sseService.sendSseEvent("Bank Transfer of Order ID " + bankmsg.getOrderID() + " is updated to: " + bankmsg.getStatus());

                            // Email notification if it is a refund
                            if(bankmsg.getStatus().equals("refunded")){
                                String emailSubject = "Your order has been refunded";
                                String emailBody = "Hi Customer, \n" +
                                        "Your order below has been refunded.\n" +
                                        "Order ID: " + bankmsg.getOrderID() +
                                        "\nFrom Account: " + bankmsg.getFromAcct() +
                                        "\nTo Account: " + bankmsg.getToAcct() +
                                        "\nAmount: " + bankmsg.getAmount();
                                storeMQService.sendEmail("merchant@mail.com","customer@mail.com",emailSubject,emailBody,emailOut);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .get();
    }

    @Bean
    IntegrationFlow DTSinboundSqlMessagesFlow(JdbcChannelMessageStore deliveryToStoreMessageStore, StoreMQService storeMQService, MessageChannel emailOut, MessageChannel bankOut, OrderRepository orderRepository, SseService sseService) {
        return IntegrationFlow
                .fromSupplier(() -> deliveryToStoreMessageStore.pollMessageFromGroup("deliverytostore-queue"),
                        e -> e.poller(p -> p.fixedDelay(1000, 1000)))
                .handle(message -> {
                    String payload = (String) message.getPayload();

                    try {
                        // Deserialize the JSON array into a list of TradeDataDTO
                        List<DeliveryDataDTO> deliveryMQList = storeMQService.getDeliveryMQList(payload);

                        for(DeliveryDataDTO deliverymsg : deliveryMQList) {
                            System.out.println("Message Received from DeliveryCo: ");
                            System.out.println("Delivery ID: " + deliverymsg.getId());
                            System.out.println("Status: " + deliverymsg.getStatus());
                            System.out.println("Order ID: " + deliverymsg.getOrderID());
                            System.out.println("Received Date: " + deliverymsg.getReceiveDate());
                            System.out.println("Pick up Date: " + deliverymsg.getPickUpDate());
                            System.out.println("Sent Date: " + deliverymsg.getSentDate());
                            System.out.println("Finish Date: " + deliverymsg.getFinishDate());
                            System.out.println();

                            // Update the order in the Store Database - to do
                            Optional<Order> optionalOderData = orderRepository.findById(deliverymsg.getOrderID());
                            if(optionalOderData.isPresent()){
                                Order order = optionalOderData.get();
                                order.setDeliveryStatus(deliverymsg.getStatus());

                                orderRepository.save(order);
                            }

                            // Notify all SSE clients, in this case, the front-end react program to refresh
                            sseService.sendSseEvent("Delivery of Order ID " + deliverymsg.getOrderID() + " is updated to: " + deliverymsg.getStatus());

                            // If the delivery is failed, refund the customer
                            if(deliverymsg.getStatus().equals("failed")){
                                storeMQService.sendEmail("merchant@mail.com","customer@mail.com","Your order is cancelled",
                                        "We are sorry to tell you that the delivery of your order (order ID: " + deliverymsg.getOrderID() + ") is failed. \nThe bank transfer will be refunded shortly.",emailOut);
                                storeMQService.requestTransfer(deliverymsg.getOrderID(),-1,"toRefund",bankOut);
                            } else {
                                // Email notification to the customer on the delivery status
                                String emailSubject = "Your order is " + deliverymsg.getStatus();
                                StringBuilder emailBody = new StringBuilder("Hi Customer, " +
                                        "\nHere is the delivery update:" +
                                        "\nOrder ID: " + deliverymsg.getOrderID() +
                                        "\nDelivery Status: " + deliverymsg.getStatus());
                                if (deliverymsg.getReceiveDate() != null) {
                                    emailBody.append("\nReceived Date: ").append(deliverymsg.getReceiveDate());
                                }

                                if (deliverymsg.getPickUpDate() != null) {
                                    emailBody.append("\nPick up Date: ").append(deliverymsg.getPickUpDate());
                                }

                                if (deliverymsg.getSentDate() != null) {
                                    emailBody.append("\nSent Date: ").append(deliverymsg.getSentDate());
                                }

                                if (deliverymsg.getFinishDate() != null) {
                                    emailBody.append("\nFinish Date: ").append(deliverymsg.getFinishDate());
                                }
                                String emailBodyStr = emailBody.toString();
                                storeMQService.sendEmail("merchant@mail.com", "customer@mail.com", emailSubject, emailBodyStr, emailOut);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .get();
    }
}
