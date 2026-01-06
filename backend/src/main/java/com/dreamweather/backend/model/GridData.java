package com.dreamweather.backend.model;

import lombok.Data;

@Data
public class GridData {
	
	private String gridId;
	private String gridX;
	private String gridY;
	
    public GridData() {}

    public GridData(String gridId, String gridX, String gridY) {
        this.gridId = gridId;
        this.gridX = gridX;
        this.gridY = gridY;
    }

}
