package com.dreamweather.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamweather.backend.service.EmailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1")
public class WakeUpController {
	
	@Autowired
	private EmailService emailService;
	
    @Value("${contact.email}")
    private String contactEmail;
	
    private static final Logger log = LoggerFactory.getLogger(WakeUpController.class);

	@GetMapping("/wakeup")
	public ResponseEntity<String> wakeUp() {
		log.info("Wake-up call received");
		String trimmed = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		emailService.sendEmail(contactEmail, "Dream Weather server awake", "at " + trimmed);
		
	    return ResponseEntity.ok("Awake");
	}

}
