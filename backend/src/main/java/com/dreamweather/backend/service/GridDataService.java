package com.dreamweather.backend.service;

import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.persistence.GridDataEntity;
import com.dreamweather.backend.persistence.GridDataRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class GridDataService {
    private final GridDataRepository gridDataRepository;

    public GridDataService(GridDataRepository gridDataRepository) {
        this.gridDataRepository = gridDataRepository;
    }

    public GridDataEntity getGridData(double latitude, double longitude) {
        return gridDataRepository
                .findByLatitudeAndLongitude(latitude, longitude)
                .orElse(null);
    }

    @Transactional
    public GridDataEntity fetchAndPersistGridData(
            double latitude,
            double longitude,
            Supplier<GridData> apiSupplier) {

        GridData apiGrid = apiSupplier.get();
        if (apiGrid == null) {
            return null;
        }

        GridDataEntity entity = new GridDataEntity(
                latitude,
                longitude,
                apiGrid.getGridId(),
                apiGrid.getGridX(),
                apiGrid.getGridY()
        );

        return gridDataRepository.saveAndFlush(entity);
    }
}

