package com.dreamweather.backend.service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {
	
	public static final long serialVersionUID = 1L;
	
    public TooManyRequestsException(String message) {
        super(message);
    }
}
