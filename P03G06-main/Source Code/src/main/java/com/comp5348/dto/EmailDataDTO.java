package com.comp5348.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmailDataDTO {

    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
    private LocalDateTime sentDate;

    public EmailDataDTO(String from, String to, String subject, String body, LocalDateTime sentDate) {
        this.fromEmail = from;
        this.toEmail = to;
        this.subject = subject;
        this.body = body;
        this.sentDate = sentDate;
    }

    public EmailDataDTO() {
    }
}
