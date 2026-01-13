package com.dreamweather.backend.service;

import com.dreamweather.backend.model.GridData;
import com.dreamweather.backend.persistence.GridDataEntity;
import com.dreamweather.backend.persistence.GridDataRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class GridDataService {
    private final GridDataRepository gridDataRepository;

    public GridDataService(GridDataRepository gridDataRepository) {
        this.gridDataRepository = gridDataRepository;
    }
	
    @Transactional
    public GridDataEntity getOrCreateGridData(
            double latitude,
            double longitude,
            Supplier<GridData> apiSupplier) {

        Optional<GridDataEntity> existing =
            gridDataRepository.findByLatitudeAndLongitude(latitude, longitude);

        if (existing.isPresent()) {
            return existing.get();
        }

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

        return gridDataRepository.save(entity);
    }
}
