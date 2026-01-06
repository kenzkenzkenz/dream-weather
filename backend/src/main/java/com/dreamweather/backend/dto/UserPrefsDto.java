package com.dreamweather.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPrefsDto {
	
	@NotBlank
	private String region;
	
	@NotBlank
	private String precipitation;
	
	@NotBlank
	private String temperature;

}
