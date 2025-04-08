package com.comp5348.EmailService.service;

import com.comp5348.EmailService.model.EmailData;
import com.comp5348.EmailService.repository.EmailDataRepository;
import com.comp5348.dto.EmailDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EmailDataService {

    private final EmailDataRepository emailDataRepository;

    @Autowired
    public EmailDataService(EmailDataRepository emailDataRepository) {
        this.emailDataRepository = emailDataRepository;
    }

    @Transactional
    public void saveEmailData(EmailDataDTO emailDataDTO) {
        // Convert DTO to entity
        EmailData emailData = new EmailData(emailDataDTO.getFromEmail(),emailDataDTO.getToEmail(),emailDataDTO.getSubject(),emailDataDTO.getBody(), emailDataDTO.getSentDate());
        // Save entity to the database
        emailDataRepository.save(emailData);
    }
}
