package com.dreamweather.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1")
public class WakeUpController {
	
    private static final Logger log = LoggerFactory.getLogger(WakeUpController.class);

	@GetMapping("/wakeup")
	public ResponseEntity<String> wakeUp() {
		log.info("Wake-up call received");
	    return ResponseEntity.ok("Awake");
	}

}
