package com.dreamweather.backend.dto;

import com.dreamweather.backend.model.Country;
import com.dreamweather.backend.model.Forecast;

import lombok.Data;

@Data
public class LiveStreamDto {
	private String slug;
	private String title;
	private String description;
	private String city;
	private String latitude;
	private String longitude;
	private String stream_type;
	private String stream_url;
	private Country country;
	private Forecast forecast;
}
