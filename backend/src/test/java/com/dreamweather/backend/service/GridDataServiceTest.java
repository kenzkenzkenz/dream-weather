package com.dreamweather.backend.service;

import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.persistence.GridDataEntity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class GridDataServiceTest {

    @Autowired
    private GridDataService gridDataService;

    @Test
    public void testFetchAndPersistGridData_createsAndReturnsGridData() {
        double lat = 39.2608439;
        double lon = -94.5197018;
        String gridId = "TEST123";
        String gridX = "50";
        String gridY = "75";
        
        // Fetch and persist GridData
        GridDataEntity grid = gridDataService.fetchAndPersistGridData(lat, lon, () -> {
            GridData gd = new GridData();
            gd.setGridId(gridId);
            gd.setGridX(gridX);
            gd.setGridY(gridY);
            return gd;
        });

        // Assertions
        assertNotNull(grid, "GridData should not be null");
        assertNotNull(grid.getGridId(), "GridId should not be null");
        assertNotNull(grid.getGridX(), "GridX should not be null");
        assertNotNull(grid.getGridY(), "GridY should not be null");

        // Fetch again to check if it's persisted
        GridDataEntity grid2 = gridDataService.getGridData(lat, lon);
        
        // Assertions
        assertNotNull(grid2, "GridDataEntity should be retrieved from DB");
        assertEquals(grid.getGridId(), grid2.getGridId(), "GridId should be the same");
        assertEquals(grid.getGridX(), grid2.getGridX(), "GridX should be the same");
        assertEquals(grid.getGridY(), grid2.getGridY(), "GridY should be the same");
    }

    @Test
    public void testGetGridData_withDifferentCoordinates() {
        double lat = 40.712776;
        double lon = -74.005974;
        String gridId = "DIFF123";
        String gridX = "60";
        String gridY = "80";

        // Create and persist new GridData
        GridDataEntity grid = gridDataService.fetchAndPersistGridData(lat, lon, () -> {
            GridData gd = new GridData();
            gd.setGridId(gridId);
            gd.setGridX(gridX);
            gd.setGridY(gridY);
            return gd;
        });

        // Assertions
        assertNotNull(grid, "GridData should be persisted");
        assertEquals(gridId, grid.getGridId(), "GridId should match the persisted value");
        assertEquals(gridX, grid.getGridX(), "GridX should match the persisted value");
        assertEquals(gridY, grid.getGridY(), "GridY should match the persisted value");
    }
}
