package com.dreamweather.backend.cache;

import com.dreamweather.backend.model.Location;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebcamCache {

    // Cache key: country code, value: list of Location objects
    private final Cache<String, List<Location>> webcamsCache;
    private static final Logger log = LoggerFactory.getLogger(WebcamCache.class);


    public WebcamCache() {
    	webcamsCache = Caffeine.newBuilder()
                .maximumSize(50)                   // max 50 country entries
                .expireAfterWrite(60, TimeUnit.MINUTES) // expire after 60 minutes
                .build();
    }

    public List<Location> getValidWebcams(String countryCode) {
    	log.info("Checking webcam cache for country code: {}", countryCode);
        return webcamsCache.getIfPresent(countryCode);
    }

    public void putValidWebcams(String countryCode, List<Location> webcams) {
    	log.info("Storing {} webcams in cache for country code: {}", webcams.size(), countryCode);
    	webcamsCache.put(countryCode, webcams);
    }
}
