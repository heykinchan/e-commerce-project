package com.comp5348.EmailService.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter @Setter
@Entity
public class EmailData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
    private LocalDateTime sentDate;

    public EmailData(String from, String to, String subject, String body, LocalDateTime sentDate) {
        this.fromEmail = from;
        this.toEmail = to;
        this.subject = subject;
        this.body = body;
        this.sentDate = sentDate;
    }

    public EmailData() {

    }
}
