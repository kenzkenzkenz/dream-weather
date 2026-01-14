package com.dreamweather.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import com.dreamweather.backend.dto.LocationDto;
import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.Forecast;
import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.model.Location;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.persistence.GridDataEntity;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private WebcamService webcamService;

    @MockBean
    private GridDataService gridDataService;

    @SuppressWarnings("unchecked")
	@Test
    void testFindLocationDataByCountry_returnsMatchingDto() throws Exception {
        // --- User preferences ---
        UserPrefs prefs = new UserPrefs();
        Country country = new Country();
        country.setName("United States");
        country.setIso_code("US");
        prefs.setCountry(country);
        prefs.setTemperature("cold");
        prefs.setPrecipitation("none");

        // --- Mock forecast ---
        Forecast mockForecast = new Forecast();
        mockForecast.setTemperature(50);
        mockForecast.setShortForecast("Sunny with clear skies");

        // --- Mock webcam ---
        Location mockWebcam = new Location();
        mockWebcam.setLatitude("39.0");
        mockWebcam.setLongitude("-94.0");
        mockWebcam.setStreamType("youtube");
        mockWebcam.setTitle("Test Cam");
        mockWebcam.setSlug("slug1");
        mockWebcam.setCountry(country);

        List<Location> webcams = List.of(mockWebcam);
        when(webcamService.fetchWebcams(prefs)).thenReturn(webcams);

        // --- Mock weather service ---
        when(weatherService.findGridDataByCoordinates(anyString(), anyString()))
                .thenReturn(new GridData("GRID123", "10", "20"));
        when(weatherService.findForecastByGridData(anyString(), anyString(), anyString()))
                .thenReturn(mockForecast);
        when(weatherService.isWeatherMatch(any(), any())).thenReturn(true);

        // --- Mock GridDataService to execute the lambda ---
        when(gridDataService.getGridData(
                anyDouble(),
                anyDouble()
        )).thenReturn(null); // Return null initially to trigger fetching and persisting new grid data

        when(gridDataService.fetchAndPersistGridData(
                anyDouble(),
                anyDouble(),
                any(Supplier.class)
        )).thenAnswer(invocation -> {
            double lat = invocation.getArgument(0);
            double lon = invocation.getArgument(1);
            Supplier<GridData> supplier = invocation.getArgument(2);

            GridData gd = supplier.get(); // <-- use get() to execute the lambda
            return new GridDataEntity(lat, lon, gd.getGridId(), gd.getGridX(), gd.getGridY());
        });

        // --- Call service ---
        LocationDto dto = locationService.findLocationDataByCountry(prefs);

        // --- Verify ---
        verify(weatherService, atLeastOnce()).findGridDataByCoordinates(anyString(), anyString());
        verify(weatherService, atLeastOnce()).findForecastByGridData(anyString(), anyString(), anyString());
        verify(gridDataService, atLeastOnce()).fetchAndPersistGridData(anyDouble(), anyDouble(), any(Supplier.class));

        // --- Assertions ---
        assertNotNull(dto, "Should return a LocationDto");
        assertEquals("United States", dto.getCountry().getName());
        assertEquals("Test Cam", dto.getTitle());
        assertEquals("slug1", dto.getSlug());
        assertNotNull(dto.getForecast(), "Forecast should be set in DTO");
        assertEquals(50, dto.getForecast().getTemperature());
    }

}
