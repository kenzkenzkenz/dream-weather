package com.dreamweather.backend.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GridDataRepository extends JpaRepository<GridDataEntity, Long> {
	
	Optional<GridDataEntity> findByLatitudeAndLongitude(double latitude, double longitude);
	
}
