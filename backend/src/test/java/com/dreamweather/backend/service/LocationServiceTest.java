package com.dreamweather.backend.service;

import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.UserPrefs;
import com.dreamweather.backend.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private GridDataService gridDataService;

    @Mock
    private WebcamService webcamService;

    private UserPrefs prefs;

    @BeforeEach
    void setUp() {
        // Setup user preferences for testing
        prefs = new UserPrefs();
        Country country = new Country();
        country.setName("United States");
        country.setIso_code("US");
        prefs.setCountry(country);
        prefs.setTemperature("cold");
        prefs.setPrecipitation("none");
    }
    
    private List<Map<String, Object>> getTestData() {
    	
    	List<Map<String, Object>> fetchedWebcams;
        Map<String, Object> location1 = createMockLocation("Location 1", "35.0", "-100.0", "youtube");
        Map<String, Object> location2 = createMockLocation("Location 2", "40.0", "-95.0", "iframe");
        Map<String, Object> location3 = createMockLocation("Location 3", "42.0", "-110.0", "youtube");
        Map<String, Object> location4 = createMockLocation("Location 4", "37.5", "-90.0", "iframe");
        Map<String, Object> location5 = createMockLocation("Location 5", "33.0", "-80.0", "youtube");
        Map<String, Object> location6 = createMockLocation("Location 6", "44.0", "-75.0", "iframe");//
        Map<String, Object> location7 = createMockLocation("Location 7", "41.5", "-85.0", "youtube");
        Map<String, Object> location8 = createMockLocation("Location 8", "36.0", "-120.0", "iframe");
        Map<String, Object> location9 = createMockLocation("Location 9", "39.0", "-70.0", "youtube");
        Map<String, Object> location10 = createMockLocation("Location 10", "38.0", "-60.0", "iframe");
        Map<String, Object> location11 = createMockLocation("Location 11", "30.0", "-50.0", "youtube");
        Map<String, Object> location12 = createMockLocation("Location 12", "45.0", "-40.0", "iframe");//
        Map<String, Object> location13 = createMockLocation("Location 13", "32.0", "-30.0", "youtube");
        Map<String, Object> location14 = createMockLocation("Location 14", "34.0", "-20.0", "iframe");
        Map<String, Object> location15 = createMockLocation("Location 15", "43.0", "-10.0", "youtube");//
        Map<String, Object> location16 = createMockLocation("Location 16", "31.0", "0.0", "iframe");
        Map<String, Object> location17 = createMockLocation("Location 17", "46.0", "10.0", "youtube");//
        Map<String, Object> location18 = createMockLocation("Location 18", "29.0", "20.0", "iframe");
        Map<String, Object> location19 = createMockLocation("Location 19", "47.0", "30.0", "youtube");//
        Map<String, Object> location20 = createMockLocation("Location 20", "28.0", "40.0", "iframe");

        fetchedWebcams = List.of(
                location1, location2, location3, location4, location5, location6, location7, 
                location8, location9, location10, location11, location12, location13, location14,
                location15, location16, location17, location18, location19, location20
        );
        return fetchedWebcams;
    }

    @Test
    void testSortLocationsByPreference_withTop5() {
    	List<Map<String, Object>> fetchedWebcams = getTestData();
        
        // Call the service method to sort locations
        List<Location> sortedLocations = new ArrayList<>(locationService.sortLocationsByPreference(convertToLocationList(fetchedWebcams), prefs));

        // Assertions
        assertNotNull(sortedLocations);
        assertTrue(sortedLocations.size() > 0, "List should have locations");

        // Check that at least the top 5 locations are present in the final list
        List<String> top5Titles = List.of("Location 6", "Location 12", "Location 15", "Location 17", "Location 19");

        // Ensure all 5 top locations are present in the shuffled list (not in specific order)
        long topLocationsCount = sortedLocations.stream()
                .filter(loc -> top5Titles.contains(loc.getTitle()))
                .count();

        // Assert that at least 5 of the top locations are present
        assertTrue(topLocationsCount >= 5, "At least 5 top locations should be present in the final list");
    }

    // Helper method to create mock locations in Map<String, Object> format
    private Map<String, Object> createMockLocation(String title, String lat, String lon, String streamType) {
        return Map.of(
                "title", title,
                "latitude", lat,
                "longitude", lon,
                "stream_type", streamType
        );
    }

    // Helper method to convert List<Map<String, Object>> to List<Location>
    private List<Location> convertToLocationList(List<Map<String, Object>> mapLocations) {
        return new ArrayList<>(mapLocations.stream()
                .map(this::mapToLocation)
                .collect(Collectors.toList())); // Ensure it's mutable
    }

    // Helper method to map Map<String, Object> to Location
    private Location mapToLocation(Map<String, Object> data) {
        Country country = new Country();
        country.setName("United States");
        country.setIso_code("US");

        Location location = new Location();
        location.setTitle((String) data.get("title"));
        location.setLatitude((String) data.get("latitude"));
        location.setLongitude((String) data.get("longitude"));
        location.setStreamType((String) data.get("stream_type"));
        location.setSlug((String) data.get("slug"));
        location.setDescription((String) data.get("description"));
        location.setCity((String) data.get("city"));
        location.setPermalink((String) data.get("permalink"));
        location.setCountry(country);
        return location;
    }
}
