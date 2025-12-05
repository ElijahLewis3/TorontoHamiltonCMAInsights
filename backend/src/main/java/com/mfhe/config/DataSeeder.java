package com.mfhe.config;

import com.mfhe.domain.EmploymentObservation;
import com.mfhe.domain.HousingObservation;
import com.mfhe.domain.Region;
import com.mfhe.repository.EmploymentObservationRepository;
import com.mfhe.repository.HousingObservationRepository;
import com.mfhe.repository.RegionRepository;
import com.mfhe.service.StatCanFetcherService;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RegionRepository regionRepo;
    private final EmploymentObservationRepository employmentRepo;
    private final HousingObservationRepository housingRepo;
    private final StatCanFetcherService statCanFetcher;

    public DataSeeder(RegionRepository regionRepo,
                      EmploymentObservationRepository employmentRepo,
                      HousingObservationRepository housingRepo,
                      StatCanFetcherService statCanFetcher) {
        this.regionRepo = regionRepo;
        this.employmentRepo = employmentRepo;
        this.housingRepo = housingRepo;
        this.statCanFetcher = statCanFetcher;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data seeding...");

        Region toronto = regionRepo.findByCode("535")
                .orElseGet(() -> regionRepo.save(createRegion("Toronto", "535")));
        Region hamilton = regionRepo.findByCode("537")
                .orElseGet(() -> regionRepo.save(createRegion("Hamilton", "537")));
        log.info("Regions ready: Toronto (id={}), Hamilton (id={})", toronto.getId(), hamilton.getId());

        Map<String, Region> regionCache = new HashMap<>();
        regionCache.put("toronto", toronto);
        regionCache.put("hamilton", hamilton);

        if (employmentRepo.count() == 0) {
            log.info("Employment table is empty. Attempting StatsCan fetch...");
            try {
                statCanFetcher.fetchEmploymentData();
                log.info("Employment data loaded from StatsCan.");
            } catch (Exception e) {
                log.warn("StatsCan employment fetch failed, falling back to CSV: {}", e.getMessage());
                loadEmploymentFromCsv(regionCache);
            }
        } else {
            log.info("Employment data already present ({} rows), skipping seed.", employmentRepo.count());
        }

        if (housingRepo.count() == 0) {
            log.info("Housing table is empty. Attempting StatsCan fetch...");
            try {
                statCanFetcher.fetchHousingData();
                log.info("Housing data loaded from StatsCan.");
            } catch (Exception e) {
                log.warn("StatsCan housing fetch failed, falling back to CSV: {}", e.getMessage());
                loadHousingFromCsv(regionCache);
            }
        } else {
            log.info("Housing data already present ({} rows), skipping seed.", housingRepo.count());
        }

        log.info("Data seeding complete.");
    }

    private void loadEmploymentFromCsv(Map<String, Region> regionCache) {
        try {
            ClassPathResource resource = new ClassPathResource("data/employment.csv");
            try (CSVReader csv = new CSVReaderBuilder(
                    new InputStreamReader(resource.getInputStream())).build()) {

                String[] header = csv.readNext();
                List<EmploymentObservation> observations = new ArrayList<>();

                String[] row;
                while ((row = csv.readNext()) != null) {
                    if (row.length < 4) continue;

                    String refDate = row[0].trim();
                    String geo = row[1].trim();
                    String naics = row[2].trim();
                    String valueStr = row[3].trim();

                    Region region = matchRegion(geo, regionCache);
                    if (region == null || valueStr.isEmpty()) continue;

                    EmploymentObservation obs = new EmploymentObservation();
                    obs.setRegion(region);
                    obs.setRefDate(refDate);
                    obs.setNaicsIndustry(naics);
                    obs.setValue(Double.parseDouble(valueStr));
                    observations.add(obs);
                }

                employmentRepo.saveAll(observations);
                log.info("Loaded {} employment observations from CSV fallback.", observations.size());
            }
        } catch (Exception e) {
            log.error("Failed to load employment data from CSV fallback", e);
        }
    }

    private void loadHousingFromCsv(Map<String, Region> regionCache) {
        try {
            ClassPathResource resource = new ClassPathResource("data/housing.csv");
            try (CSVReader csv = new CSVReaderBuilder(
                    new InputStreamReader(resource.getInputStream())).build()) {

                String[] header = csv.readNext();
                List<HousingObservation> observations = new ArrayList<>();

                String[] row;
                while ((row = csv.readNext()) != null) {
                    if (row.length < 5) continue;

                    String refDate = row[0].trim();
                    String geo = row[1].trim();
                    String dwellingType = row[2].trim();
                    String metric = row[3].trim();
                    String valueStr = row[4].trim();

                    Region region = matchRegion(geo, regionCache);
                    if (region == null || valueStr.isEmpty()) continue;

                    HousingObservation obs = new HousingObservation();
                    obs.setRegion(region);
                    obs.setRefDate(refDate);
                    obs.setDwellingType(dwellingType);
                    obs.setMetric(metric);
                    obs.setValue(Double.parseDouble(valueStr));
                    observations.add(obs);
                }

                housingRepo.saveAll(observations);
                log.info("Loaded {} housing observations from CSV fallback.", observations.size());
            }
        } catch (Exception e) {
            log.error("Failed to load housing data from CSV fallback", e);
        }
    }

    private Region matchRegion(String geo, Map<String, Region> cache) {
        if (geo == null) return null;
        String lower = geo.toLowerCase();
        if (lower.contains("toronto")) return cache.get("toronto");
        if (lower.contains("hamilton")) return cache.get("hamilton");
        return null;
    }

    private Region createRegion(String name, String code) {
        Region r = new Region();
        r.setName(name);
        r.setCode(code);
        return r;
    }
}
