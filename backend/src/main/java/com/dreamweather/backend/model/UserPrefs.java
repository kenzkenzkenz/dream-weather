package com.dreamweather.backend.model;

import lombok.Data;

@Data
public class UserPrefs {
	
	private Country country;
	private String precipitation;
	private String temperature;

}
