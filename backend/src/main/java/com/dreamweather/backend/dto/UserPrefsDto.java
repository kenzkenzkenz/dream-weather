package com.dreamweather.backend.dto;

import com.dreamweather.backend.model.Country;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPrefsDto {
	
	@NotBlank
	private Country country;
	
	@NotBlank
	private String precipitation;
	
	@NotBlank
	private String temperature;

}
