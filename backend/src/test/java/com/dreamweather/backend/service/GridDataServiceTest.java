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
    public void testGetOrFetchGridData_createsAndReturnsGridData() {
        double lat = 39.2608439;
        double lon = -94.5197018;
        String gridId = "TEST123";
        String gridX = "50";
        String gridY = "75";
        
        GridDataEntity grid = gridDataService.getOrCreateGridData(lat, lon, () -> {
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
        
        GridDataEntity grid2 = gridDataService.getOrCreateGridData(lat, lon, () -> {
            GridData gd = new GridData();
            gd.setGridId(gridId);
            gd.setGridX(gridX);
            gd.setGridY(gridY);
            return gd;
        });

        assertEquals(grid.getGridId(), grid2.getGridId(), "Should return same GridId from cache/DB");
    }

    @Test
    public void testGetOrFetchGridData_withDifferentCoordinates() {
        double lat = 40.712776;
        double lon = -74.005974;
        String gridId = "DIFF123";
        String gridX = "60";
        String gridY = "80";

        GridDataEntity grid = gridDataService.getOrCreateGridData(lat, lon, () -> {
            GridData gd = new GridData();
            gd.setGridId(gridId);
            gd.setGridX(gridX);
            gd.setGridY(gridY);
            return gd;
        });


        assertNotNull(grid);
        assertNotNull(grid.getGridId());
    }
}