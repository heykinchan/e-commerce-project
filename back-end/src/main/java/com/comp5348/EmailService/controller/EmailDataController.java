package com.comp5348.EmailService.controller;

import com.comp5348.EmailService.repository.EmailDataRepository;
import com.comp5348.EmailService.service.EmailDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailDataController {

    private final EmailDataService emailDataService;
    private final EmailDataRepository emailDataRepository;

    @Autowired
    public EmailDataController(EmailDataService emailDataService, EmailDataRepository emailDataRepository) {
        this.emailDataService = emailDataService;
        this.emailDataRepository = emailDataRepository;
    }

    @DeleteMapping("/clearDB")
    public void clearDeliveryCoDB(){
        emailDataRepository.deleteAll();
    }
}
