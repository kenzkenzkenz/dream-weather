package com.dreamweather.backend.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class WeatherGovAuthProvider {

    private final String appName;
    private final String contactEmail;

    public WeatherGovAuthProvider(
        @Value("${application.name}") String appName,
        @Value("${contact.email}") String contactEmail
    ) {
        this.appName = appName;
        this.contactEmail = contactEmail;
    }

    public HttpEntity<Void> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", appName + " (" + contactEmail + ")");
        headers.set("Accept", "application/geo+json");
        return new HttpEntity<>(headers);
    }
}
