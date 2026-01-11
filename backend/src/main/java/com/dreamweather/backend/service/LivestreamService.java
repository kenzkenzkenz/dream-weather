package com.dreamweather.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dreamweather.backend.dto.LiveStreamDto;
import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.Forecast;
import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.model.Webcam;

@Service
public class LivestreamService {

    private static final Logger log = LoggerFactory.getLogger(LivestreamService.class);

    private final WeatherService weatherService;
    private final SkippedStreamService skippedStreamService;
    private final EmailService emailService;
    private final String apiKey;
    private final String contactEmail;

    public LivestreamService(
            WeatherService weatherService,
            SkippedStreamService skippedStreamService,
            EmailService emailService,
            @Value("${openwebcamdb.api.key}") 
            String apiKey,
            @Value("${contact.email}") String contactEmail) {
        this.weatherService = weatherService;
        this.skippedStreamService = skippedStreamService;
        this.emailService = emailService;
        this.apiKey = apiKey;
        this.contactEmail = contactEmail;
    }
	
	@SuppressWarnings("unchecked")
	public LiveStreamDto findLivestreamDataByCountry(UserPrefs prefs) {
	    log.info("Fetching data for country: {} ", prefs.getCountry().getName());

	    int totalPages = 5;       // Approx. number of pages for US webcams
	    int perPage = 100;        // Max results per page
	    int subsetSize = 20;      // How many webcams to consider
	    int weatherCalls = 0;

	    // Pick a random page
	    int randomPage = new Random().nextInt(totalPages) + 1;
	    String url = "https://openwebcamdb.com/api/v1/countries/US?per_page=" 
	                 + perPage + "&page=" + randomPage;

	    RestTemplate restTemplate = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Authorization", "Bearer " + apiKey);
	    HttpEntity<Void> entity = new HttpEntity<>(headers);
	    ResponseEntity<Map<String, Object>> response = null;
	    
	    try {
	        response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            entity,
	            new ParameterizedTypeReference<>() {}
	        );
	    } catch (HttpClientErrorException e) {
	        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
	            log.error("Rate limit exceeded when accessing OpenWebcamDB API");
	            throw new TooManyRequestsException("OpenWebcamDB API rate limit exceeded");
	        }
	        throw e;
	    }

	    Map<String, Object> body = response.getBody();
	    List<Map<String, Object>> webcamsFromResponse = body != null
	            ? (List<Map<String, Object>>) body.getOrDefault("webcams", Collections.emptyList())
	            : Collections.emptyList();

	    if (webcamsFromResponse.isEmpty()) {
	        log.info("No webcams found for {}", prefs.getCountry().getName());
	        return null;
	    }
	    
	    List<String> skippedSlugs = new ArrayList<>();

	    // Convert to Webcam objects
	    List<Webcam> webcams = webcamsFromResponse.stream()
		    .filter(data -> {
		        String slug = (String) data.get("slug");
		        String reason = skippedStreamService.getSkipReason(slug); 

		        if (reason != null) {
		            skippedSlugs.add(slug);
		            //log.info("Skipped slug {} due to {}", slug, reason);
		            return false; // filter out
		        }

		        return true; // keep
		    })
		    .map(data -> {
		        Webcam w = new Webcam();
		        w.setSlug((String) data.get("slug"));
		        w.setTitle((String) data.get("title"));
		        w.setDescription((String) data.get("description"));
		        w.setCity((String) data.get("city"));
		        w.setLatitude((String) data.get("latitude"));
		        w.setLongitude((String) data.get("longitude"));
		        w.setPermalink((String) data.get("permalink"));
		        w.setStreamType((String) data.get("stream_type"));
		        return w;
		    }).collect(Collectors.toList());
	    
    	// Log the skipped slugs
	    if (!skippedSlugs.isEmpty()) {
	        log.info("Skipped webcams: {}", String.join(", ", skippedSlugs));
	    }

	    // Shuffle and pick a subset
	    Collections.shuffle(webcams);
	    List<Webcam> webcamsSubset = webcams.subList(0, Math.min(subsetSize, webcams.size()));

	    // For each webcam, get gridData and forecast; return first match
	    for (Webcam cam : webcamsSubset) {
	        String lat = cam.getLatitude();
	        String lon = cam.getLongitude();

	        if (lat == null || lon == null || cam.getStreamType() == null 
	        		|| (!cam.getStreamType().equals("youtube") && !cam.getStreamType().equals("iframe"))) {
	            continue; // skip webcams with missing coordinates
	        }

	        GridData grid = weatherService.findGridDataByCoordinates(lat, lon);
	        weatherCalls++;

	        if (grid != null) {
	            Forecast forecast = weatherService.findForecastByGridData(
	                    grid.getGridId(), grid.getGridX(), grid.getGridY());
	            weatherCalls++;

	            if (weatherService.findWeatherMatch(forecast, prefs)) {
	            	
	            	log.info("Made {} weather calls for precip {} and temp {}. Found matching weather at {}",
	            	        weatherCalls, prefs.getPrecipitation(), prefs.getTemperature(), cam.getTitle());

	            	cam.setForecast(forecast);
	                
	                String stream = fetchWebcamStreamUrl(cam.getSlug());
	                
	                String emailBody = String.format(
	                        "Dream Weather Request Summary:%n" +
	                        "--------MATCH FOUND -------%n" +
	                        "Weather calls made: %d%n" +
	                        "Precipitation preference: %s%n" +
	                        "Temperature preference: %s%n",
	                        weatherCalls,
	                        prefs.getPrecipitation(),
	                        prefs.getTemperature()
	                );

	                emailService.sendEmail(contactEmail, "Dream Weather Requested", emailBody);

	                return convertWebcamToDto(cam, prefs.getCountry(), stream);
	            } else {
	                log.info("No weather match for {}", cam.getTitle());
	            }
	        } else {
	            log.info("No grid data for {}", cam.getTitle());
	        }
	    }

	    log.info("Made {} weather calls for precip {} and temp {}. Found no matching locations.",
	            weatherCalls, prefs.getPrecipitation(), prefs.getTemperature());
	    
        String emailBody = String.format(
                "Dream Weather Request Summary:%n" +
                "-------MATCH NOT FOUND--------%n" +
                "Weather calls made: %d%n" +
                "Precipitation preference: %s%n" +
                "Temperature preference: %s%n",
                weatherCalls,
                prefs.getPrecipitation(),
                prefs.getTemperature()
        );

        emailService.sendEmail(contactEmail, "Dream Weather Requested", emailBody);

	    return null;
	}
	
	@SuppressWarnings("unchecked")
	public String fetchWebcamStreamUrl(String slug) {
	    String url = "https://openwebcamdb.com/api/v1/webcams/" + slug;

	    RestTemplate restTemplate = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Authorization", "Bearer " + apiKey);

	    HttpEntity<Void> entity = new HttpEntity<>(headers);
	    
	    try {
		    ResponseEntity<Map<String, Object>> response =
			        restTemplate.exchange(
			            url,
			            HttpMethod.GET,
			            entity,
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
	
	public LiveStreamDto convertWebcamToDto(Webcam webcam, Country country, String streamUrl) {
		LiveStreamDto dto = new LiveStreamDto();
		dto.setSlug(webcam.getSlug());
		dto.setTitle(webcam.getTitle());
		dto.setDescription(webcam.getDescription());
		dto.setCity(webcam.getCity());
		dto.setLatitude(webcam.getLatitude());
		dto.setLongitude(webcam.getLongitude());
		dto.setStream_url(webcam.getPermalink());
		dto.setCountry(country);
		dto.setForecast(webcam.getForecast());
		dto.setStream_type(webcam.getStreamType());
		dto.setStream_url(streamUrl);
		return dto;
	}
}
