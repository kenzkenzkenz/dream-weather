package com.dreamweather.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.service.LivestreamService;

@RestController
@RequestMapping("/api/v1")
public class LivestreamController {
	
    @Value("${frontend.url}")
    private String frontendUrl;
	
	@Autowired
	private LivestreamService livestreamService;
	
	@CrossOrigin(origins = "${frontend.url}")
	@PostMapping("/livestream/match")
	public ResponseEntity<LivestreamResponse> getLivestreamDataByCountry(@RequestBody UserPrefs prefs) {
	    LiveStreamDto webcam = livestreamService.findLivestreamDataByCountry(prefs);

	    if (webcam != null) {
	        return ResponseEntity.ok(
	            new LivestreamResponse(true, "Match found", webcam)
	        );
	    } else {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	            .body(new LivestreamResponse(false, "No matches found", null));
	    }
	}
}