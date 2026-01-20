package com.dreamweather.backend.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;


@Component
public class OpenWebcamAuthProvider {

    private final String apiKey1;
    private final String apiKey2;

    public OpenWebcamAuthProvider(
        @Value("${openwebcamdb.api.key1}") String apiKey1,
        @Value("${openwebcamdb.api.key2}") String apiKey2
    ) {
        this.apiKey1 = apiKey1;
        this.apiKey2 = apiKey2;
    }
    
    private HttpEntity<Void> entity(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        return new HttpEntity<>(headers);
    }

    public HttpEntity<Void> entity1() {
        return entity(apiKey1);
    }
    
    public HttpEntity<Void> entity2() {
        return entity(apiKey2);
    }
}
