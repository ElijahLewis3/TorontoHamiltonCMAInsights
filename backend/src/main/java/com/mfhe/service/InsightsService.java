package com.mfhe.service;

import com.mfhe.dto.*;
import com.mfhe.domain.EmploymentObservation;
import com.mfhe.domain.HousingObservation;
import com.mfhe.domain.Region;
import com.mfhe.repository.EmploymentObservationRepository;
import com.mfhe.repository.HousingObservationRepository;
import com.mfhe.repository.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    private final RegionRepository regionRepo;
    private final EmploymentObservationRepository employmentRepo;
    private final HousingObservationRepository housingRepo;

    public InsightsService(RegionRepository regionRepo,
                           EmploymentObservationRepository employmentRepo,
                           HousingObservationRepository housingRepo) {
        this.regionRepo = regionRepo;
        this.employmentRepo = employmentRepo;
        this.housingRepo = housingRepo;
    }

    public Optional<CombinedTimeSeries> getCombinedTimeSeries(String regionCode) {
        return regionRepo.findByCode(regionCode).map(region -> {
            List<HousingObservation> housingObs =
                    housingRepo.findByRegionAndMetricOrderByRefDate(region, "Starts");
            Map<String, Double> housingByDate = housingObs.stream()
                    .collect(Collectors.groupingBy(
                            HousingObservation::getRefDate,
                            LinkedHashMap::new,
                            Collectors.summingDouble(HousingObservation::getValue)));

            List<TimeSeriesPoint> housingStarts = housingByDate.entrySet().stream()
                    .map(e -> new TimeSeriesPoint(e.getKey(), e.getValue()))
                    .toList();

            List<EmploymentObservation> empObs =
                    employmentRepo.findByRegionOrderByRefDate(region);
            Map<String, Double> empByDate = empObs.stream()
                    .collect(Collectors.groupingBy(
                            EmploymentObservation::getRefDate,
                            LinkedHashMap::new,
                            Collectors.summingDouble(EmploymentObservation::getValue)));

            List<TimeSeriesPoint> employment = empByDate.entrySet().stream()
                    .map(e -> new TimeSeriesPoint(e.getKey(), e.getValue()))
                    .toList();

            return new CombinedTimeSeries(housingStarts, employment);
        });
    }

    public Optional<List<NaicsShare>> getNaicsBreakdown(String regionCode) {
        return regionRepo.findByCode(regionCode).map(region -> {
            List<EmploymentObservation> allObs =
                    employmentRepo.findByRegionOrderByRefDate(region);

            if (allObs.isEmpty()) return List.<NaicsShare>of();

            String latestDate = allObs.get(allObs.size() - 1).getRefDate();

            return allObs.stream()
                    .filter(o -> o.getRefDate().equals(latestDate))
                    .map(o -> new NaicsShare(o.getNaicsIndustry(), o.getValue()))
                    .toList();
        });
    }

    public Optional<List<HousingCategorySummary>> getHousingCategories(String regionCode) {
        return regionRepo.findByCode(regionCode).map(region -> {
            List<HousingObservation> allObs =
                    housingRepo.findByRegionOrderByRefDate(region);

            Map<String, double[]> grouped = new LinkedHashMap<>();
            for (HousingObservation obs : allObs) {
                double[] sums = grouped.computeIfAbsent(obs.getDwellingType(), k -> new double[2]);
                if ("Starts".equals(obs.getMetric())) {
                    sums[0] += obs.getValue();
                } else if ("Completions".equals(obs.getMetric())) {
                    sums[1] += obs.getValue();
                }
            }

            return grouped.entrySet().stream()
                    .map(e -> new HousingCategorySummary(e.getKey(), e.getValue()[0], e.getValue()[1]))
                    .toList();
        });
    }

    public Optional<List<HeatmapCell>> getHeatmapData(String regionCode) {
        return regionRepo.findByCode(regionCode).map(region -> {
            List<HousingObservation> startsObs =
                    housingRepo.findByRegionAndMetricOrderByRefDate(region, "Starts");

            return startsObs.stream()
                    .map(o -> new HeatmapCell(o.getRefDate(), o.getDwellingType(), o.getValue()))
                    .toList();
        });
    }
}
