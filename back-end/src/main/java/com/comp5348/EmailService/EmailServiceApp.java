package com.comp5348.EmailService;

import com.comp5348.EmailService.model.EmailData;
import com.comp5348.EmailService.service.EmailDataService;
import com.comp5348.dto.EmailDataDTO;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.messaging.support.MessageBuilder;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@PropertySource("classpath:application-emailservice.properties")
@SpringBootApplication(scanBasePackages = "com.comp5348.EmailService")
public class EmailServiceApp {

	public static void main(String[] args) {
		SpringApplication.run(EmailServiceApp.class, args);
	}

	// Bean definition for JdbcChannelMessageStore
	// Stores and retrieves messages from the database
	@Bean
	JdbcChannelMessageStore messageStore(DataSource dataSource) {
		var messageStore = new JdbcChannelMessageStore(dataSource);
		messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
		messageStore.setTablePrefix("EMAILDATA_");
		return messageStore;
	}

	// Bean definition for IntegrationFlow
	// Handles the inbound messages from the message store
	@Bean
	IntegrationFlow inboundSqlMessagesFlow(JdbcChannelMessageStore messageStore, EmailDataService emailDataService) {
		return IntegrationFlow
				.fromSupplier(() -> messageStore.pollMessageFromGroup("email-queue"),
						e -> e.poller(p -> p.fixedDelay(1000, 1000)))
				.handle(message -> {
					String payload = (String) message.getPayload();

					try {
						// Deserialize the JSON array into a list of TradeDataDTO
						ObjectMapper mapper = new ObjectMapper();
						mapper.registerModule(new JavaTimeModule());
						List<EmailDataDTO> emailDataList = mapper.readValue(payload, new TypeReference<List<EmailDataDTO>>() {});

						// Save each TradeDataDTO using the service
						emailDataList.forEach(emailDataService::saveEmailData);
						for(EmailDataDTO emailData : emailDataList) {
							System.out.println("From: " + emailData.getFromEmail());
							System.out.println("To: " + emailData.getToEmail());
							System.out.println("Subject: " + emailData.getSubject());
							System.out.println("Body: " + emailData.getBody());
							System.out.println("Email Sent Date: " + emailData.getSentDate());
							System.out.println();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				})
				.get();
	}
}
