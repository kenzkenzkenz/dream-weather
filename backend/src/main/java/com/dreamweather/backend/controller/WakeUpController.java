package com.dreamweather.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1")
public class WakeUpController {
	
    private static final Logger log = LoggerFactory.getLogger(WakeUpController.class);

    @RequestMapping(
            value = "/wakeup",
            method = { RequestMethod.GET, RequestMethod.HEAD }
        )
	public ResponseEntity<String> wakeUp(HttpServletRequest request) {
    	String method = request.getMethod();
		String trimmed = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		log.info("Wake-up call received via " + method + " at " + trimmed);
	    return ResponseEntity.ok("Awake");
	}
}
