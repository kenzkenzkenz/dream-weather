package com.dreamweather.backend.service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.dreamweather.backend.model.Stream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SkippedStreamService {
    private static final Logger log = LoggerFactory.getLogger(SkippedStreamService.class);
    private final Set<String> deadSlugs = new HashSet<>();
    private final Set<String> wildcardSlugs = new HashSet<>();

    public SkippedStreamService(ObjectMapper objectMapper) throws IOException {
        // ---------- Load dead streams ----------
        String deadPath = System.getenv("DEAD_STREAMS_JSON");
        List<Stream> deadStreams;

        if (deadPath != null && !deadPath.isBlank()) {
            // PROD: read JSON from secret file path
            File deadFile = new File(deadPath);
            if (deadFile.exists()) {
                deadStreams = objectMapper.readValue(deadFile, new TypeReference<List<Stream>>() {});
            } else {
                log.warn("Dead streams file not found at {}", deadPath);
                deadStreams = List.of();
            }
        } else {
            // LOCAL
            ClassPathResource resource = new ClassPathResource("deadstreams.json");
            if (resource.exists()) {
                deadStreams = objectMapper.readValue(
                    resource.getInputStream(), new TypeReference<List<Stream>>() {}
                );
            } else {
                deadStreams = List.of();
            }
        }

        deadStreams.forEach(s -> deadSlugs.add(s.getSlug()));
        log.info("Loaded {} dead streams", deadSlugs.size());

        // ---------- Load wildcard streams ----------
        String wildcardPath = System.getenv("WILDCARD_STREAMS_JSON");
        List<Stream> wildcardStreams;

        if (wildcardPath != null && !wildcardPath.isBlank()) {
            File wildcardFile = new File(wildcardPath);
            if (wildcardFile.exists()) {
                wildcardStreams = objectMapper.readValue(wildcardFile, new TypeReference<List<Stream>>() {});
            } else {
                log.warn("Wildcard streams file not found at {}", wildcardPath);
                wildcardStreams = List.of();
            }
        } else {
            // LOCAL
            ClassPathResource resource = new ClassPathResource("wildcards.json");
            if (resource.exists()) {
                wildcardStreams = objectMapper.readValue(
                    resource.getInputStream(), new TypeReference<List<Stream>>() {}
                );
            } else {
                wildcardStreams = List.of();
            }
        }

        wildcardStreams.forEach(s -> wildcardSlugs.add(s.getSlug()));
        log.info("Loaded {} wildcard streams", wildcardSlugs.size());
    }

    public boolean isSkipped(String slug) {
        return deadSlugs.contains(slug) || wildcardSlugs.contains(slug);
    }
    
    public String getSkipReason(String slug) {
        if (deadSlugs.contains(slug)) return "broken";
        if (wildcardSlugs.contains(slug)) return "wildcard";
        return null;
    }
}