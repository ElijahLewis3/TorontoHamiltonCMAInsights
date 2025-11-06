package com.mfhe.repository;

import com.mfhe.domain.HousingObservation;
import com.mfhe.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface HousingObservationRepository extends JpaRepository<HousingObservation, Long> {

    List<HousingObservation> findByRegionAndMetricOrderByRefDate(Region region, String metric);

    List<HousingObservation> findByRegionOrderByRefDate(Region region);

    @Query("SELECT MAX(h.refDate) FROM HousingObservation h WHERE h.region = :region")
    Optional<String> findLatestRefDateByRegion(@Param("region") Region region);

    @Transactional
    void deleteByRegion(Region region);
}
