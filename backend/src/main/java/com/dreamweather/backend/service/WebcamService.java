package com.dreamweather.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dreamweather.backend.model.Location;
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

    @SuppressWarnings("unchecked")
    public List<Location> fetchWebcams(String countryCode) {
	    int totalPages = 5;       // Approx. number of pages for US webcams
	    int perPage = 100;        // Max results per page
	    int subsetSize = 20;      // How many webcams to consider

	    // Pick a random page
	    int randomPage = new Random().nextInt(totalPages) + 1;
	    String url = "https://openwebcamdb.com/api/v1/countries/" 
	    		+ countryCode + "?per_page=" + perPage + "&page=" + randomPage;

        ResponseEntity<Map<String, Object>> response;

        try {
            response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                authProvider.entity(),
                new ParameterizedTypeReference<>() {}
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("OpenWebcamDB rate limit exceeded");
                throw new TooManyRequestsException("OpenWebcamDB API rate limit exceeded");
            }
            throw e;
        }

        Map<String, Object> body = response.getBody();
        if (body == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> webcamsRaw =
                (List<Map<String, Object>>) body.getOrDefault("webcams", Collections.emptyList());
        
        if (webcamsRaw.isEmpty()) {
        	log.warn("No locations found for country code: " + countryCode);
			return Collections.emptyList();
        }
        
        List<String> skippedSlugs = new ArrayList<>();     

            List<Location> locations = webcamsRaw.stream()
            		.filter(data -> {
            			String slug = (String) data.get("slug");
            String reason = skippedStreamService.getSkipReason(slug);
            
            if(reason != null) {
            	skippedSlugs.add(slug);
            	return false;
            }
            return true;
            		})
                .map(data -> {
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
                })
                .collect(Collectors.toList());
            
            //Log the skipped slugs
            if (!skippedSlugs.isEmpty()) {
            	log.info("Skipped locations: {}", String.join(", ",  skippedSlugs));;
            }

            // Shuffle and pick a subset
            Collections.shuffle(locations);
            
            if (locations.isEmpty()) return Collections.emptyList();
            return locations.subList(0, Math.min(subsetSize, locations.size()));
        }
    
	@SuppressWarnings("unchecked")
	public String fetchStreamUrl(String slug) {
	    String url = "https://openwebcamdb.com/api/v1/webcams/" + slug;
	    
	    try {
		    ResponseEntity<Map<String, Object>> response =
			        restTemplate.exchange(
			            url,
			            HttpMethod.GET,
			            authProvider.entity(),
			            new ParameterizedTypeReference<Map<String, Object>>() {}
			        );

			    Map<String, Object> body = response.getBody();
			    if (body == null) return null;

			    Map<String, Object> data = (Map<String, Object>) body.get("data");
			    if (data == null) return null;

			    return (String) data.get("stream_url");
	    	
	    } catch (HttpClientErrorException e) {
	    	if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
	    		log.error("Rate limit exceeded when accessing OpenWebcamDB API for stream URL");
	    		return null;
	    	} else {
	    		log.error("Error fetching webcam stream URL: " + e.getStatusCode());
	    		return null;
	    	}
	    }
	}
}
