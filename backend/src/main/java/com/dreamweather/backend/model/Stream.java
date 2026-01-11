package com.dreamweather.backend.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Stream {
	@NotNull
	private String slug;
	
	@NotNull
	private String stream_url;

}
