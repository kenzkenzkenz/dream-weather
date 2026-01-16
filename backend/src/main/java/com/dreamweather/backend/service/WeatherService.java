package com.dreamweather.backend.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dreamweather.backend.http.WeatherGovAuthProvider;
import com.dreamweather.backend.model.Forecast;
import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.model.UserPrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WeatherService {
	
	private static final String[] RAIN = {"rain"};
	private static final String[] SNOW = {"snow"};
	private static final String[] RAIN_WORDS = {"rain", "showers", "shower", "drizzle", "thunderstorm", "thunder", "storms"};
	private static final String[] SNOW_WORDS = {"snow", "sleet", "blizzard", "flurries"};
	private static final int HOT_THRESHOLD = 65; // Fahrenheit
	
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    
    private final RestTemplate restTemplate;
    private final WeatherGovAuthProvider authProvider;
    private final AtomicInteger totalWeatherCalls = new AtomicInteger(0);
    private final ThreadLocal<Integer> requestWeatherCalls = ThreadLocal.withInitial(() -> 0);
    
    public WeatherService(
    		RestTemplate restTemplate,
    		WeatherGovAuthProvider authProvider) {
			this.restTemplate = restTemplate;
			this.authProvider = authProvider;
		}
    
    public int getAndResetRequestCount() {
        int count = requestWeatherCalls.get();
        requestWeatherCalls.remove();
        return count;
    }

    private void incrementCall() {
        totalWeatherCalls.incrementAndGet();
        requestWeatherCalls.set(requestWeatherCalls.get() + 1);
    }

	public GridData findGridDataByCoordinates(String lat, String lon) {
		String url = "https://api.weather.gov/points/" + lat + "," + lon;
		
		Long start = System.nanoTime();
		
		try {
			log.info("Calling Weather API to fetch grid data for coords: {}/{}...", lat, lon);
			
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        authProvider.entity(),
                        new ParameterizedTypeReference<>() {}
                    );
            incrementCall();
            
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("Weather.gov grid call took {} ms.", durationMs);
			
			if (!response.getStatusCode().is2xxSuccessful()) {
			    log.error("Non-success HTTP status {} for lat={}, lon={}!", 
			            response.getStatusCode(), lat, lon);
			    return null;
			}
		
			Map<String, Object> body = response.getBody();
	
			if (body != null && body.get("properties") != null) {
			    @SuppressWarnings("unchecked")
				Map<String, Object> properties = (Map<String, Object>) body.get("properties");
			    String gridId = (String) properties.get("gridId");
			    Object gridX = properties.get("gridX");
			    Object gridY = properties.get("gridY");

			    if (gridId != null && gridX != null && gridY != null) {
			        return new GridData(gridId, gridX.toString(), gridY.toString());
			    } else {
			        log.error("Incomplete grid data for lat={}, lon={}!", lat, lon);
			    }
			}
			return null;

		} catch (HttpStatusCodeException e) {
	        long durationMs = (System.nanoTime() - start) / 1_000_000;
	        log.error(
	            "Weather.gov HTTP error {} after {} ms for lat={}, lon={}: {}!",
	            e.getStatusCode(), durationMs, lat, lon, e.getResponseBodyAsString()
	        );
		        return null;
		    } catch (RestClientException e) {
		        long durationMs = (System.nanoTime() - start) / 1_000_000;
		        log.error(
		            "Weather.gov request failed after {} ms for lat={}, lon={}: {}!",
		            durationMs, lat, lon, e.getMessage()
		        );
		        return null;
		    }
	}
	
	public Forecast findForecastByGridData(String gridId, String gridX, String gridY) {
	    String url = "https://api.weather.gov/gridpoints/" + gridId + "/" + gridX + "," + gridY + "/forecast";
	    
		Long start = System.nanoTime();
	    
	    try {
			log.info("Calling Weather API to fetch forecast for grid {}/{}...", gridId, gridX + "," + gridY);
			
			
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        authProvider.entity(),
                        new ParameterizedTypeReference<>() {}
                    );
            incrementCall();
            
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("Weather API forecast call took {} ms.", durationMs);
	        
	        Map<String, Object> body = response.getBody();
	        if (body == null) {
	            log.error("Empty forecast response!");
	            return null;
	        }

	        @SuppressWarnings("unchecked")
			Map<String, Object> properties = (Map<String, Object>) body.get("properties");
	        if (properties == null) {
	            log.error("Missing properties in forecast response!");
	            return null;
	        }

	        @SuppressWarnings("unchecked")
			List<Map<String, Object>> periods =
	            (List<Map<String, Object>>) properties.get("periods");
	        if (periods == null || periods.isEmpty()) {
	            log.error("No forecast periods returned!");
	            return null;
	        }

	        Map<String, Object> firstPeriod = periods.get(0);

	        Forecast forecast = new Forecast();
	        forecast.setTemperature((Integer) firstPeriod.get("temperature"));
	        forecast.setShortForecast((String) firstPeriod.get("shortForecast"));

	        return forecast;

	    } catch (HttpStatusCodeException e) {
	    	long durationMs = (System.nanoTime() - start) / 1_000_000;
	        log.error("Weather API HTTP error {} failed after {} ms for grid {}/{}: {}.", 
	        		e.getStatusCode(), durationMs, gridId, gridX + "," + gridY, e.getResponseBodyAsString());
	        
	    } catch (RestClientException e) {
	    	long durationMs = (System.nanoTime() - start) / 1_000_000;
	        log.error("Weather.gov request failed after {} ms for grid {}/{}: {}.", 
	                durationMs, gridId, gridX + "," + gridY, e.getMessage());
	    }
	    
	    return null;
	}

	
	public boolean isWeatherMatch(Forecast forecast, UserPrefs prefs) {
	    if (forecast == null || prefs == null || forecast.getShortForecast() == null || prefs.getPrecipitation() == null) {
	        return false;
	    }

	    String shortForecast = forecast.getShortForecast().toLowerCase();
	    String tempPref = prefs.getTemperature().toLowerCase();
	    String precipPref = prefs.getPrecipitation().toLowerCase();

	    boolean isTempMatch = isTemperatureMatch(forecast.getTemperature(), tempPref, HOT_THRESHOLD);
	    boolean isPrecipMatch = isPrecipitationMatch(shortForecast, precipPref);

	    log.debug("Evaluating weather match: Forecast='{}', Temp={}, UserPrefs: TempPref='{}', PrecipPref='{}' "
	    		+ "-> TempMatch={}, PrecipMatch={}...",
	              shortForecast, forecast.getTemperature(), tempPref, precipPref, isTempMatch, isPrecipMatch);

	    return isTempMatch && isPrecipMatch;
	}

	private boolean isTemperatureMatch(int temp, String tempPref, int hotThreshold) {
	    return (tempPref.equals("hot") && temp >= hotThreshold)
	        || (tempPref.equals("cold") && temp < hotThreshold);
	}

	private boolean isPrecipitationMatch(String forecast, String precipPref) {
	    forecast = forecast.toLowerCase();

	    boolean containsRainOnly = Arrays.stream(RAIN).anyMatch(forecast::contains);
	    boolean containsSnowOnly = Arrays.stream(SNOW).anyMatch(forecast::contains);
	    boolean containsRain = Arrays.stream(RAIN_WORDS).anyMatch(forecast::contains);
	    boolean containsSnow = Arrays.stream(SNOW_WORDS).anyMatch(forecast::contains);

	    return (precipPref.equals("rain") && containsRain && !containsSnowOnly)
	        || (precipPref.equals("snow") && containsSnow && !containsRainOnly)
	        || (precipPref.equals("none") && !containsRain && !containsSnow);
	}
}
