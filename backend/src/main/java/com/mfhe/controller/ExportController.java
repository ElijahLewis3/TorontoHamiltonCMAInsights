package com.mfhe.controller;

import com.mfhe.domain.EmploymentObservation;
import com.mfhe.domain.HousingObservation;
import com.mfhe.domain.Region;
import com.mfhe.repository.EmploymentObservationRepository;
import com.mfhe.repository.HousingObservationRepository;
import com.mfhe.repository.RegionRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final RegionRepository regionRepo;
    private final EmploymentObservationRepository employmentRepo;
    private final HousingObservationRepository housingRepo;

    public ExportController(RegionRepository regionRepo,
                            EmploymentObservationRepository employmentRepo,
                            HousingObservationRepository housingRepo) {
        this.regionRepo = regionRepo;
        this.employmentRepo = employmentRepo;
        this.housingRepo = housingRepo;
    }

    @GetMapping("/employment/{code}")
    public ResponseEntity<byte[]> exportEmployment(@PathVariable String code) {
        Optional<Region> regionOpt = regionRepo.findByCode(code);
        if (regionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Region region = regionOpt.get();
        List<EmploymentObservation> observations =
                employmentRepo.findByRegionOrderByRefDate(region);

        StringBuilder sb = new StringBuilder();
        sb.append("REF_DATE,GEO,NAICS_INDUSTRY,VALUE\n");
        for (EmploymentObservation obs : observations) {
            sb.append(escapeCsv(obs.getRefDate())).append(',')
              .append(escapeCsv(region.getName())).append(',')
              .append(escapeCsv(obs.getNaicsIndustry())).append(',')
              .append(obs.getValue()).append('\n');
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"employment_" + code + ".csv\"")
                .body(sb.toString().getBytes());
    }

    @GetMapping("/housing/{code}")
    public ResponseEntity<byte[]> exportHousing(@PathVariable String code) {
        Optional<Region> regionOpt = regionRepo.findByCode(code);
        if (regionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Region region = regionOpt.get();
        List<HousingObservation> observations =
                housingRepo.findByRegionOrderByRefDate(region);

        StringBuilder sb = new StringBuilder();
        sb.append("REF_DATE,GEO,DWELLING_TYPE,METRIC,VALUE\n");
        for (HousingObservation obs : observations) {
            sb.append(escapeCsv(obs.getRefDate())).append(',')
              .append(escapeCsv(region.getName())).append(',')
              .append(escapeCsv(obs.getDwellingType())).append(',')
              .append(escapeCsv(obs.getMetric())).append(',')
              .append(obs.getValue()).append('\n');
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"housing_" + code + ".csv\"")
                .body(sb.toString().getBytes());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
