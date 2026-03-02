package com.dreamweather.backend.service;

import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.persistence.GridDataEntity;
import com.dreamweather.backend.persistence.GridDataRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GridDataServiceTest {

    @Mock
    private GridDataRepository gridDataRepository;

    @InjectMocks
    private GridDataService gridDataService;

    @Test
    void testFetchAndPersistGridData_createsAndReturnsGridData() {
        double lat = 39.26;
        double lon = -94.51;

        GridData apiGrid = new GridData();
        apiGrid.setGridId("TEST123");
        apiGrid.setGridX("50");
        apiGrid.setGridY("75");

        when(gridDataRepository.saveAndFlush(any(GridDataEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GridDataEntity result = gridDataService.fetchAndPersistGridData(
                lat,
                lon,
                () -> apiGrid
        );

        assertNotNull(result);
        assertEquals("TEST123", result.getGridId());
        assertEquals("50", result.getGridX());
        assertEquals("75", result.getGridY());

        verify(gridDataRepository, times(1)).saveAndFlush(any(GridDataEntity.class));
    }

    @Test
    void testGetGridData_returnsEntityWhenFound() {
        double lat = 40.7;
        double lon = -74.0;

        GridDataEntity entity = new GridDataEntity(lat, lon, "ID1", "10", "20");

        when(gridDataRepository.findByLatitudeAndLongitude(lat, lon))
                .thenReturn(Optional.of(entity));

        GridDataEntity result = gridDataService.getGridData(lat, lon);

        assertNotNull(result);
        assertEquals("ID1", result.getGridId());
    }
}