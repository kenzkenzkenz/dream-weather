package com.dreamweather.backend.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dreamweather.backend.dto.LocationDto;
import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.Forecast;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.model.Location;
import com.dreamweather.backend.persistence.GridDataEntity;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final WeatherService weatherService;
    private final GridDataService gridDataService;
    private final WebcamService webcamService;

    public LocationService(
            WeatherService weatherService,
            GridDataService gridDataService,
            WebcamService webcamService
            ) {
        this.weatherService = weatherService;
        this.gridDataService = gridDataService;
        this.webcamService = webcamService;
    }
    
	public LocationDto findLocationDataByCountry(UserPrefs prefs) {
	    log.info("Fetching data for country: {} ", prefs.getCountry().getName());
	    
	    List<Location> locations = webcamService.fetchWebcams(prefs.getCountry().getIso_code());

	    // For each webcam, get gridData and forecast; return first match
	    for (Location loc : locations) {
	        String lat = loc.getLatitude();
	        String lon = loc.getLongitude();

	        if (lat == null || lon == null || loc.getStreamType() == null 
	        		|| (!loc.getStreamType().equals("youtube") && !loc.getStreamType().equals("iframe"))) {
	            continue; // skip webcams with missing coordinates
	        }
	        double latVal, lonVal;
	        try {
	            latVal = Double.parseDouble(lat);
	            lonVal = Double.parseDouble(lon);
	        } catch (NumberFormatException e) {
	            log.warn("Invalid coordinates for {}", loc.getSlug());
	            continue;
	        }

        	GridDataEntity gridEntity = gridDataService.getOrCreateGridData(
        		    latVal,
        		    lonVal,
        		    () -> {
        		        return weatherService.findGridDataByCoordinates(lat, lon);
        		    }
        		);

        		if (gridEntity== null ||
        			gridEntity.getGridId() == null ||
        		    gridEntity.getGridX() == null ||
        		    gridEntity.getGridY() == null) {
        		    log.warn("Invalid grid data for {}", loc.getSlug());
        		    continue;
        		} 
        		try {
		        Forecast forecast = weatherService.findForecastByGridData(
		        	    gridEntity.getGridId(),
		        	    gridEntity.getGridX(),
		        	    gridEntity.getGridY());
		            
		            if (weatherService.isWeatherMatch(forecast, prefs)) {
		            	
		            	log.info("Found matching weather for precip {} and temp {} at {}",
		            	        prefs.getPrecipitation(), prefs.getTemperature(), loc.getTitle());

		            	loc.setForecast(forecast);
		                
		                String stream = webcamService.fetchStreamUrl(loc.getSlug());

		                return convertWebcamToDto(loc, prefs.getCountry(), stream);
		            } else {
		                log.info("Not a weather match at {}", loc.getSlug());
		            }
	        } catch (Exception e) {
	            log.warn("Forecast lookup failed for {}", loc.getSlug(), e);
	            continue;
	        }
	    }

	    log.info("Found no locations for precip {} and temp {}.",
	            prefs.getPrecipitation(), prefs.getTemperature());

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
}
