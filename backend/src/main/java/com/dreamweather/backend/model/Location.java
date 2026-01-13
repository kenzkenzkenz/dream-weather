package com.dreamweather.backend.model;

import lombok.Data;

@Data
public class Webcam {
	
	private String slug;
	private String title;
	private String description;
	private String city;
	private String latitude;
	private String longitude;
	private String permalink;
	private Country country;
	private GridData gridData;
	private Forecast forecast;
	private String streamType;

}
