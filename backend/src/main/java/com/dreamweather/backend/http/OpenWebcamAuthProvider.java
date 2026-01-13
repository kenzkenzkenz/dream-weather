package com.dreamweather.backend.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class OpenWebcamAuthProvider {

    private final String apiKey;

    public OpenWebcamAuthProvider(
        @Value("${openwebcamdb.api.key}") String apiKey
    ) {
        this.apiKey = apiKey;
    }

    public HttpEntity<Void> entity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        return new HttpEntity<>(headers);
    }
}
