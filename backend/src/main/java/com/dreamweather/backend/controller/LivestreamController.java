package com.dreamweather.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamweather.backend.dto.LiveStreamDto;
import com.dreamweather.backend.dto.LivestreamResponse;
import com.dreamweather.backend.model.Stream;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.service.EmailService;
import com.dreamweather.backend.service.LivestreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "${frontend.url}")
public class LivestreamController {
    private static final Logger log = LoggerFactory.getLogger(LivestreamController.class);
	
	private LivestreamService livestreamService;
	private EmailService emailService;
	private String contactEmail;
	
    public LivestreamController(
            LivestreamService livestreamService,
            EmailService emailService,
            @Value("${contact.email}") String contactEmail
    ) {
        this.livestreamService = livestreamService;
        this.emailService = emailService;
        this.contactEmail = contactEmail;
    }
	
    @PostMapping("/livestream/match")
    public ResponseEntity<LivestreamResponse> getLivestreamMatch(@Valid @RequestBody UserPrefs prefs) {
        LiveStreamDto webcam = livestreamService.findLivestreamDataByCountry(prefs);

        // Return response
        if (webcam != null) {
            return ResponseEntity.ok(new LivestreamResponse(true, "Match found", webcam));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new LivestreamResponse(false, "No matches found", null));
        }
    }
	
	@PostMapping("/livestream/report")
	public ResponseEntity<String> sendLivestreamReport(@Valid @RequestBody Stream stream) {
	    log.info("Livestream reported by a user. slug: {}, url: {}", 
	    		stream.getSlug(), stream.getStream_url());
	    
		emailService.sendEmail(contactEmail, "Livestream Reported by User", 
				"slug: " + stream.getSlug() + "\n" + 
				"url:" + stream.getStream_url());
		
		return ResponseEntity.ok("Reported");
	}
}