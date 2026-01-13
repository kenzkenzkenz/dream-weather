package com.dreamweather.backend.persistence;

import jakarta.persistence.*;

@Entity
@Table(
    name = "grid_data",
    uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude"})
)
public class GridDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private String gridId;

    @Column(nullable = false)
    private String gridX;

    @Column(nullable = false)
    private String gridY;

    public GridDataEntity() {
    }

    public GridDataEntity(
            double latitude,
            double longitude,
            String gridId,
            String gridX,
            String gridY
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.gridId = gridId;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Long getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getGridId() { return gridId; }
    public String getGridX() { return gridX; }
    public String getGridY() { return gridY; }
    
    public void setId(Long id) { this.id = id; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setGridId(String gridId) { this.gridId = gridId; }
    public void setGridX(String gridX) { this.gridX = gridX; }
    public void setGridY(String gridY) { this.gridY = gridY; }
    
}