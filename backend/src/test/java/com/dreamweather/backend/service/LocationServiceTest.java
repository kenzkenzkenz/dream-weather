package com.dreamweather.backend.service;

import com.dreamweather.backend.cache.WebcamCache;
import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock private WeatherService weatherService;
    @Mock private GridDataService gridDataService;
    @Mock private WebcamService webcamService;
    @Mock private WebcamCache webcamCache;

    private LocationService locationService;
    private UserPrefs prefs;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(
                weatherService,
                gridDataService,
                webcamService,
                webcamCache,
                30 // weatherCap for testing
        );

        // Setup user preferences
        prefs = new UserPrefs();
        Country country = new Country();
        country.setName("United States");
        country.setIso_code("US");
        prefs.setCountry(country);
        prefs.setTemperature("cold");
        prefs.setPrecipitation("none");
    }

    @Test
    void testSortLocationsByPreference_withTop5() {
        List<Map<String, Object>> fetchedWebcams = getTestData();

        List<Location> sortedLocations = new ArrayList<>(
                locationService.sortLocationsByPreference(convertToLocationList(fetchedWebcams), prefs)
        );

        assertNotNull(sortedLocations);
        assertTrue(sortedLocations.size() > 0, "List should have locations");

        List<String> top5Titles = List.of(
                "Location 6", "Location 12", "Location 15", "Location 17", "Location 19"
        );

        long topLocationsCount = sortedLocations.stream()
                .filter(loc -> top5Titles.contains(loc.getTitle()))
                .count();

        assertTrue(topLocationsCount >= 5, "At least 5 top locations should be present");
    }

    // Helper to generate mock location data
    private List<Map<String, Object>> getTestData() {
        return List.of(
            createMockLocation("Location 1", "35.0", "-100.0", "youtube"),
            createMockLocation("Location 2", "40.0", "-95.0", "iframe"),
            createMockLocation("Location 3", "42.0", "-110.0", "youtube"),
            createMockLocation("Location 4", "37.5", "-90.0", "iframe"),
            createMockLocation("Location 5", "33.0", "-80.0", "youtube"),
            createMockLocation("Location 6", "44.0", "-75.0", "iframe"),
            createMockLocation("Location 7", "41.5", "-85.0", "youtube"),
            createMockLocation("Location 8", "36.0", "-120.0", "iframe"),
            createMockLocation("Location 9", "39.0", "-70.0", "youtube"),
            createMockLocation("Location 10", "38.0", "-60.0", "iframe"),
            createMockLocation("Location 11", "30.0", "-50.0", "youtube"),
            createMockLocation("Location 12", "45.0", "-40.0", "iframe"),
            createMockLocation("Location 13", "32.0", "-30.0", "youtube"),
            createMockLocation("Location 14", "34.0", "-20.0", "iframe"),
            createMockLocation("Location 15", "43.0", "-10.0", "youtube"),
            createMockLocation("Location 16", "31.0", "0.0", "iframe"),
            createMockLocation("Location 17", "46.0", "10.0", "youtube"),
            createMockLocation("Location 18", "29.0", "20.0", "iframe"),
            createMockLocation("Location 19", "47.0", "30.0", "youtube"),
            createMockLocation("Location 20", "28.0", "40.0", "iframe")
        );
    }

    private Map<String, Object> createMockLocation(String title, String lat, String lon, String streamType) {
        return Map.of(
                "title", title,
                "latitude", lat,
                "longitude", lon,
                "stream_type", streamType
        );
    }

    private List<Location> convertToLocationList(List<Map<String, Object>> mapLocations) {
        return new ArrayList<>(mapLocations.stream()
                .map(this::mapToLocation)
                .collect(Collectors.toList()));
    }

    private Location mapToLocation(Map<String, Object> data) {
        Country country = new Country();
        country.setName("United States");
        country.setIso_code("US");

        Location location = new Location();
        location.setTitle((String) data.get("title"));
        location.setLatitude((String) data.get("latitude"));
        location.setLongitude((String) data.get("longitude"));
        location.setStreamType((String) data.get("stream_type"));
        location.setCountry(country);
        return location;
    }
}