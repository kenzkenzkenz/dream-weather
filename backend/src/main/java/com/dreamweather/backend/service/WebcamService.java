package com.dreamweather.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dreamweather.backend.model.Location;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.exception.TooManyRequestsException;
import com.dreamweather.backend.http.OpenWebcamAuthProvider;

@Service
public class WebcamService {

    private static final Logger log = LoggerFactory.getLogger(WebcamService.class);

    private final RestTemplate restTemplate;
    private final OpenWebcamAuthProvider authProvider;
    private final SkippedStreamService skippedStreamService;

    public WebcamService(
    		RestTemplate restTemplate,
            OpenWebcamAuthProvider authProvider,
            SkippedStreamService skippedStreamService
    		) {
        this.restTemplate = restTemplate;
        this.authProvider = authProvider;
        this.skippedStreamService = skippedStreamService;
    }
    
    private Map<String, Object> fetchPage(String countryCode, int perPage, int page) {
    	log.info("Calling OpenWebcamDB API for country: {}, page: {}...", countryCode, page);
        String url = "https://openwebcamdb.com/api/v1/countries/"
                + countryCode
                + "?per_page=" + perPage
                + "&page=" + page;

        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    authProvider.entity(),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("OpenWebcamDB rate limit exceeded for fetching locations!");
                throw new TooManyRequestsException("OpenWebcamDB API rate limit exceeded");
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchWebcams(UserPrefs prefs) {
        int totalPages = 3; // Number of pages to fetch (out of 5)
        int perPage = 100;
        String countryCode = prefs.getCountry().getIso_code();
        
        List<Integer> allPages = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            allPages.add(i);
        }
        
        Collections.shuffle(allPages);
        List<Integer> selectedPages = allPages.subList(0, totalPages);
        log.info("OpenWebcamDB pages to be fetched are: {}. ", selectedPages);
        
        List<Map<String, Object>> allWebcams = new ArrayList<>();
        selectedPages.forEach(pg -> {
            try {
                // Fetch data for each page linearly
                Map<String, Object> pageData = fetchPage(countryCode, perPage, pg);
                if (pageData != null && pageData.containsKey("webcams")) {
                    List<Map<String, Object>> webcams = (List<Map<String, Object>>) pageData.get("webcams");
                    if (webcams != null) {
                        allWebcams.addAll(webcams);  // Collect webcams from each page
                    }
                }
            } catch (TooManyRequestsException e) {
        	    log.error("OpenWebcamDB rate limit exceeded for page {}! Stopping further requests.", pg);
        	    throw e;
            } catch (Exception e) {
                log.error("Error fetching page {}!", pg, e.getMessage());
                throw e;
            }
        });

        return allWebcams;
    }
    
    public List<Location> filterSkipLocations(List<Map<String, Object>> fetchedWebcams, UserPrefs prefs) {
        if (fetchedWebcams == null || fetchedWebcams.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Filter out skipped webcams
        List<String> skippedSlugs = new ArrayList<>();
        List<Location> validLocations = fetchedWebcams.stream()
            .filter(data -> {
                String slug = (String) data.get("slug");
                String reason = skippedStreamService.getSkipReason(slug);
                String streamType = (String) data.get("stream_type");
                
                // Skip if there's a skip reason or if stream type is invalid (not youtube or iframe)
                if (reason != null || !("youtube".equals(streamType) || "iframe".equals(streamType))) {
                    skippedSlugs.add(slug);
                    return false; // skip this webcam
                }
                // Check if latitude and longitude are valid
                String lat = (String) data.get("latitude");
                String lon = (String) data.get("longitude");
                try {
                    Double.parseDouble(lat);
                    Double.parseDouble(lon);
                } catch (NumberFormatException e) {
                    skippedSlugs.add(slug);
                    log.warn("Invalid coordinates for {}: Latitude: {}, Longitude: {}!", slug, lat, lon);
                    return false; // Skip this webcam
                }

                return true; // Keep this webcam
            })
            .map(this::mapToLocation) // Convert to Location object
            .collect(Collectors.toList());

        if (!skippedSlugs.isEmpty()) {
            log.info("Skipping {} locations due to dead or irrelevant livestreams.", skippedSlugs.size());
        }

        return validLocations;
    }
    
    private Location mapToLocation(Map<String, Object> data) {
        Location l = new Location();
        l.setSlug((String) data.get("slug"));
        l.setTitle((String) data.get("title"));
        l.setDescription((String) data.get("description"));
        l.setCity((String) data.get("city"));
        l.setLatitude((String) data.get("latitude"));
        l.setLongitude((String) data.get("longitude"));
        l.setPermalink((String) data.get("permalink"));
        l.setStreamType((String) data.get("stream_type"));
        return l;
    }

	@SuppressWarnings("unchecked")
	public String fetchStreamUrl(String slug) {
	    String url = "https://openwebcamdb.com/api/v1/webcams/" + slug;
	    log.info("Calling OpenWebcamDB API for stream URL of webcam: {}...", slug);
	    try {
		    ResponseEntity<Map<String, Object>> response =
			        restTemplate.exchange(
			            url,
			            HttpMethod.GET,
			            authProvider.entity(),
			            new ParameterizedTypeReference<Map<String, Object>>() {}
			        );
		    
	        Map<String, Object> body = response.getBody();
	        return Optional.ofNullable(body)
	                .map(b -> (Map<String, Object>) b.get("data"))
	                .map(data -> (String) data.get("stream_url"))
	                .orElse(null);
	    	
	    } catch (HttpClientErrorException e) {
	    	if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
	    		log.error("OpenWebcamDB API rate limit exceeded for fetching stream URL!");
	    		throw new TooManyRequestsException("OpenWebcamDB API rate limit exceeded");
	    	} else {
	    		log.error("Error fetching webcam stream URL: {}!", e.getStatusCode());
	    		throw e;
	    	}
	    }
	}
}
