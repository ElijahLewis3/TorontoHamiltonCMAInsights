package com.mfhe.repository;

import com.mfhe.domain.EmploymentObservation;
import com.mfhe.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EmploymentObservationRepository extends JpaRepository<EmploymentObservation, Long> {

    List<EmploymentObservation> findByRegionOrderByRefDate(Region region);

    List<EmploymentObservation> findByRegionAndRefDateOrderByNaicsIndustry(Region region, String refDate);

    @Query("SELECT MAX(e.refDate) FROM EmploymentObservation e WHERE e.region = :region")
    Optional<String> findLatestRefDateByRegion(@Param("region") Region region);

    @Transactional
    void deleteByRegion(Region region);
}
