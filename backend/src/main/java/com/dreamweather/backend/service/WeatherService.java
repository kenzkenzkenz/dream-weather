package com.dreamweather.backend.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dreamweather.backend.model.Forecast;
import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.model.UserPrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WeatherService {
	
	private static final String[] RAIN_WORDS = {"rain", "showers", "shower", "drizzle", "thunderstorm", "thunder", "storms"};
	private static final String[] SNOW_WORDS = {"snow", "sleet", "blizzard", "flurries"};
	private static final int HOT_THRESHOLD = 67; // Fahrenheit
	
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
	
	@Value("${application.name}")
	private String appName;
	
    @Value("${contact.email}")
    private String contactEmail;
	
	public GridData findGridDataByCoordinates(String lat, String lon) {
		String url = "https://api.weather.gov/points/" + lat + "," + lon;
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", appName + " (" + contactEmail + ")");
		headers.set("Accept", "application/geo+json");
		HttpEntity<String> entity = new HttpEntity<>(headers);
		
		try {
			log.info("Requesting grid data for coordinates: {}/{}", lat, lon);
			ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
			
			if (!response.getStatusCode().is2xxSuccessful()) {
			    log.error("Non-success HTTP status {} for lat={}, lon={}", 
			            response.getStatusCode(), lat, lon);
			    return null;
			}
		
			@SuppressWarnings("unchecked")
			Map<String, Object> body = (Map<String, Object>) response.getBody();
	
			if (body != null && body.get("properties") != null) {
			    @SuppressWarnings("unchecked")
				Map<String, Object> properties = (Map<String, Object>) body.get("properties");
			    String gridId = (String) properties.get("gridId");
			    Object gridX = properties.get("gridX");
			    Object gridY = properties.get("gridY");

			    if (gridId != null && gridX != null && gridY != null) {
			        return new GridData(gridId, gridX.toString(), gridY.toString());
			    } else {
			        log.error("Incomplete grid data for lat={}, lon={}", lat, lon);
			    }
			}
			return null;

		} catch (HttpStatusCodeException e) {
		    log.error("Weather.gov HTTP error {} for lat={}, lon={}: {}", 
		            e.getStatusCode(), lat, lon, e.getResponseBodyAsString());
		        return null;
		    } catch (RestClientException e) {
		        log.error("Weather.gov request failed for lat={}, lon={}: {}", 
		                lat, lon, e.getMessage());
		        return null;
		    }
	}
	
	public Forecast findForecastByGridData(String gridId, String gridX, String gridY) {
	    String url = "https://api.weather.gov/gridpoints/" + gridId + "/" + gridX + "," + gridY + "/forecast";
	    
	    RestTemplate restTemplate = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("User-Agent", appName + " (" + contactEmail + ")");
	    headers.set("Accept", "application/geo+json");
	    HttpEntity<String> entity = new HttpEntity<>(headers);
	    
	    try {
			log.info("Requesting forecast for grid {}/{}", gridId, gridX + "," + gridY);
	        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
	        	        new ParameterizedTypeReference<Map<String, Object>>() {});
	        
	        Map<String, Object> body = response.getBody();
	        if (body == null) {
	            log.error("Empty forecast response");
	            return null;
	        }

	        @SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) body.get("properties");
	        if (properties == null) {
	            log.error("Missing properties in forecast response");
	            return null;
	        }

	        @SuppressWarnings("unchecked")
			List<Map<String, Object>> periods =
	            (List<Map<String, Object>>) properties.get("periods");
	        if (periods == null || periods.isEmpty()) {
	            log.error("No forecast periods returned");
	            return null;
	        }

	        Map<String, Object> firstPeriod = periods.get(0);

	        Forecast forecast = new Forecast();
	        forecast.setTemperature((Integer) firstPeriod.get("temperature"));
	        forecast.setShortForecast((String) firstPeriod.get("shortForecast"));

	        return forecast;

	    } catch (HttpStatusCodeException e) {
	        log.error("Weather.gov HTTP error {} for grid {}/{}: {}", 
	                e.getStatusCode(), gridId, gridX + "," + gridY, e.getResponseBodyAsString());
	    } catch (RestClientException e) {
	        log.error("Weather.gov request failed for grid {}/{}: {}", 
	                gridId, gridX + "," + gridY, e.getMessage());
	    }
	    
	    return null;
	}

	
	public boolean findWeatherMatch(Forecast forecast, UserPrefs prefs) {
	    if (forecast == null || prefs == null || forecast.getShortForecast() == null || prefs.getPrecipitation() == null) {
	        return false;
	    }

	    String shortForecast = forecast.getShortForecast().toLowerCase();
	    String tempPref = prefs.getTemperature().toLowerCase();
	    String precipPref = prefs.getPrecipitation().toLowerCase();

	    boolean isTempMatch = isTemperatureMatch(forecast.getTemperature(), tempPref, HOT_THRESHOLD);
	    boolean isPrecipMatch = isPrecipitationMatch(shortForecast, precipPref);

	    log.debug("Evaluating weather match: Forecast='{}', Temp={}, UserPrefs: TempPref='{}', PrecipPref='{}' -> TempMatch={}, PrecipMatch={}",
	              shortForecast, forecast.getTemperature(), tempPref, precipPref, isTempMatch, isPrecipMatch);

	    return isTempMatch && isPrecipMatch;
	}

	private boolean isTemperatureMatch(int temp, String tempPref, int hotThreshold) {
	    return (tempPref.equals("hot") && temp >= hotThreshold)
	        || (tempPref.equals("cold") && temp < hotThreshold);
	}

	private boolean isPrecipitationMatch(String forecast, String precipPref) {
	    forecast = forecast.toLowerCase();

	    boolean containsRain = Arrays.stream(RAIN_WORDS).anyMatch(forecast::contains);
	    boolean containsSnow = Arrays.stream(SNOW_WORDS).anyMatch(forecast::contains);

	    return (precipPref.equals("rain") && containsRain)
	        || (precipPref.equals("snow") && containsSnow)
	        || (precipPref.equals("none") && !containsRain && !containsSnow);
	}
}
