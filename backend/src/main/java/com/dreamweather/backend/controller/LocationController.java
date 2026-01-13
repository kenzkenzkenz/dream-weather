package com.dreamweather.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dreamweather.backend.dto.LocationDto;
import com.dreamweather.backend.dto.LocationResponse;
import com.dreamweather.backend.model.Stream;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.service.EmailService;
import com.dreamweather.backend.service.LocationService;
import com.dreamweather.backend.service.WeatherService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "${frontend.url}")
public class LocationController {
    private static final Logger log = LoggerFactory.getLogger(LocationController.class);
	
	private LocationService livestreamService;
	private EmailService emailService;
	private WeatherService weatherService;
	
    public LocationController(
            LocationService livestreamService,
            EmailService emailService,
            WeatherService weatherService
    ) {
        this.livestreamService = livestreamService;
        this.emailService = emailService;
        this.weatherService = weatherService;
    }
	
    @PostMapping("/livestream/match")
    public ResponseEntity<LocationResponse> getLivestreamMatch(
            @Valid @RequestBody UserPrefs prefs) {

        LocationDto webcam = livestreamService.findLocationDataByCountry(prefs);

        int weatherCallsThisRequest = weatherService.getAndResetRequestCount();
        int locationsChecked = livestreamService.getAndResetLocationCount();

        log.info("========== FINAL RESULT ==========");
        log.info("[FINAL_MATCH] Total locations checked: {}", locationsChecked);
        log.info("[FINAL_MATCH] Weather.gov API calls: {}", weatherCallsThisRequest);
        log.info("[FINAL_MATCH] User Country: {}, precip: {}, temp: {}",
                prefs.getCountry().getName(),
                prefs.getPrecipitation(),
                prefs.getTemperature());

        if (webcam == null) {
            log.info("[FINAL_MATCH] No matching webcam found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new LocationResponse(false, "No matches found", null));
        }

        log.info("[FINAL_MATCH] Matching webcam found: {}", webcam.getSlug());
        return ResponseEntity.ok(new LocationResponse(true, "Match found", webcam));
    }

	@PostMapping("/livestream/report")
	public ResponseEntity<String> sendLivestreamReport(@Valid @RequestBody Stream stream) {
	    log.info("[REPORTED] Livestream reported by a user. slug: {}, url: {}", 
	    		stream.getSlug(), stream.getStream_url());
	    
		emailService.sendEmail("Livestream Reported by User", 
				"slug: " + stream.getSlug());
		
		return ResponseEntity.ok("Reported");
	}
}