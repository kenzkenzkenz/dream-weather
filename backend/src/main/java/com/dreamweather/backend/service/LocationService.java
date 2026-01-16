package com.dreamweather.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.dreamweather.backend.cache.WebcamCache;
import com.dreamweather.backend.dto.LocationDto;
import com.dreamweather.backend.exception.TooManyRequestsException;
import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.Forecast;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.model.Location;
import com.dreamweather.backend.persistence.GridDataEntity;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private final AtomicInteger totalLocationsChecked = new AtomicInteger(0);
    private final ThreadLocal<Integer> requestLocationMatch = ThreadLocal.withInitial(() -> 0);

    private final WeatherService weatherService;
    private final GridDataService gridDataService;
    private final WebcamService webcamService;
    private final WebcamCache webcamCache;
    private final int weatherCap;

    public LocationService(
            WeatherService weatherService,
            GridDataService gridDataService,
            WebcamService webcamService,
            WebcamCache webcamCache,
            @Value("${weather.api.max:20}") int weatherCap
            ) {
        this.weatherService = weatherService;
        this.gridDataService = gridDataService;
        this.webcamService = webcamService;
        this.webcamCache = webcamCache;
        this.weatherCap = weatherCap;
    }
    
    public int getAndResetLocationCount() {
        int count = requestLocationMatch.get();
        requestLocationMatch.remove();
        return count;
    }

    private void incrementCall() {
    	totalLocationsChecked.incrementAndGet();
    	requestLocationMatch.set(requestLocationMatch.get() + 1);
    }
    
    private void sortByClimatePreference(List<Location> locations, UserPrefs prefs) {
        if ("cold".equalsIgnoreCase(prefs.getTemperature())
                || "snow".equalsIgnoreCase(prefs.getPrecipitation())) {

            locations.sort((a, b) ->
                    Double.compare(parseLat(b.getLatitude()), parseLat(a.getLatitude())));

        } else if ("hot".equalsIgnoreCase(prefs.getTemperature())) {

            locations.sort((a, b) ->
                    Double.compare(parseLat(a.getLatitude()), parseLat(b.getLatitude())));
        }
    }

    private double parseLat(String latStr) {
        try {
            return Double.parseDouble(latStr);
        } catch (NumberFormatException e) {
            return 0; // fallback
        }
    }
    
	public Optional<LocationDto> findLocationDataByCountry(UserPrefs prefs) {
        List<Location> locations = webcamCache.getValidWebcams(prefs.getCountry().getIso_code());
        
        if (locations == null) {
            log.info("Cache miss – fetching data for country: {}. ", prefs.getCountry().getName());
            
            // Fetch from API if cache miss
            List<Map<String, Object>> fetchedWebcams = webcamService.fetchWebcams(prefs);
            locations = webcamService.filterSkipLocations(fetchedWebcams, prefs);

            // Store the data in the cache
            webcamCache.putValidWebcams(prefs.getCountry().getIso_code(), locations);
        } else {
            log.info("Cache hit – using cached webcams for country: {}. ", prefs.getCountry().getName());
        }
	    
	    if(locations == null || locations.isEmpty()) {
	    	log.info("No webcams available for country {}!", prefs.getCountry().getName());
	    	return Optional.empty();
	    }
	    
	    List<Location> sortedLocations = sortLocationsByPreference(locations, prefs);
	    
	    for (Location loc : sortedLocations) {
            Double latVal = Double.parseDouble(loc.getLatitude());
            Double lonVal = Double.parseDouble(loc.getLongitude());
	    		        
	        GridDataEntity gridEntity = gridDataService.getGridData(latVal, lonVal);
	        if (gridEntity == null) {
	            gridEntity = gridDataService.fetchAndPersistGridData(
	                latVal, lonVal, () -> weatherService.findGridDataByCoordinates(loc.getLatitude(), loc.getLongitude())
	            );
	        }

	        if (gridEntity == null || gridEntity.getGridId() == null
	                || gridEntity.getGridX() == null || gridEntity.getGridY() == null) {
	            log.warn("Invalid grid data for {}!", loc.getSlug());
	            continue;
	        }

	        try {
	            Forecast forecast = weatherService.findForecastByGridData(
	                    gridEntity.getGridId(),
	                    gridEntity.getGridX(),
	                    gridEntity.getGridY());

	            incrementCall();
	            log.info("Forecast for {}: {}, {} degrees F.", loc.getSlug(), forecast.getShortForecast(), forecast.getTemperature());

	            if (weatherService.isWeatherMatch(forecast, prefs)) {
	                log.info("Found matching weather for precip {} and temp {} at {}.",
	                         prefs.getPrecipitation(), prefs.getTemperature(), loc.getSlug());

	                loc.setForecast(forecast);
	                String stream = webcamService.fetchStreamUrl(loc.getSlug());

	                return Optional.of(convertWebcamToDto(loc, prefs.getCountry(), stream));
	            } else {
	                log.info("Not a weather match at {}.", loc.getSlug());
	            }
	        } catch (TooManyRequestsException e) {
	                log.error("OpenWebcamDB API rate limit exceeded for fetching stream URL!");
	                throw e;
	            } catch (HttpClientErrorException e) {
		            log.warn("Forecast lookup failed for {}!", loc.getSlug(), e);
		            continue;
	            }
	        }
		    log.info("No locations found for precip {} and temp {}.", prefs.getPrecipitation(), prefs.getTemperature());
		    return null;
	}
	
	public LocationDto convertWebcamToDto(Location webcam, Country country, String streamUrl) {
		LocationDto dto = new LocationDto();
		dto.setSlug(webcam.getSlug());
		dto.setTitle(webcam.getTitle());
		dto.setDescription(webcam.getDescription());
		dto.setCity(webcam.getCity());
		dto.setLatitude(webcam.getLatitude());
		dto.setLongitude(webcam.getLongitude());
		dto.setCountry(country);
		dto.setForecast(webcam.getForecast());
		dto.setStream_type(webcam.getStreamType());
		dto.setStream_url(streamUrl);
		return dto;
	}
	
	public List<Location> sortLocationsByPreference(List<Location> locations, UserPrefs prefs) {
	    int halfSize = Math.max(1, locations.size() / 2);
	    sortByClimatePreference(locations, prefs);
	    
	    List<Location> topLocations = locations.stream()
	            .limit(5)
	            .collect(Collectors.toList());

	    List<Location> remainingLocations = locations.stream()
	            .skip(5)
	            .limit(Math.max(halfSize - 5, 0))
	            .collect(Collectors.toList());
	    
	    Collections.shuffle(remainingLocations, ThreadLocalRandom.current());

	    List<Location> combined = new ArrayList<>(remainingLocations);
	    combined.addAll(topLocations);

	    Collections.shuffle(combined, ThreadLocalRandom.current());
	    
	    // Return the final sublist of the shuffled list, up to a maximum of x locations
	    return combined.subList(0, Math.min(weatherCap, combined.size()));
	}

}
