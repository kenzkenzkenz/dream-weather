package com.dreamweather.backend.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPrefs {
	
	@NotNull
	private Country country;
	
	@NotNull
	private String precipitation;
	
	@NotNull
	private String temperature;

}
