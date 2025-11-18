package com.mfhe.service;

import com.mfhe.domain.EmploymentObservation;
import com.mfhe.domain.HousingObservation;
import com.mfhe.domain.Region;
import com.mfhe.repository.EmploymentObservationRepository;
import com.mfhe.repository.HousingObservationRepository;
import com.mfhe.repository.RegionRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class StatCanFetcherService {

    private static final Logger log = LoggerFactory.getLogger(StatCanFetcherService.class);

    private static final String EMPLOYMENT_ZIP_URL =
            "https://www150.statcan.gc.ca/n1/tbl/csv/14100382-eng.zip";
    private static final String HOUSING_ZIP_URL =
            "https://www150.statcan.gc.ca/n1/tbl/csv/34100143-eng.zip";

    private static final Set<String> BROAD_NAICS = Set.of(
            "Goods-producing sector",
            "Construction",
            "Manufacturing",
            "Wholesale and retail trade",
            "Transportation and warehousing",
            "Finance, insurance, real estate, rental and leasing",
            "Professional, scientific and technical services",
            "Educational services",
            "Health care and social assistance",
            "Accommodation and food services"
    );

    private static final Map<String, String> DWELLING_TYPE_MAP = Map.of(
            "Single-detached", "Single",
            "Semi-detached", "Semi-detached",
            "Row", "Row",
            "Apartment and other unit type", "Apartment",
            "Apartment", "Apartment"
    );

    private final RegionRepository regionRepo;
    private final EmploymentObservationRepository employmentRepo;
    private final HousingObservationRepository housingRepo;

    public StatCanFetcherService(RegionRepository regionRepo,
                                 EmploymentObservationRepository employmentRepo,
                                 HousingObservationRepository housingRepo) {
        this.regionRepo = regionRepo;
        this.employmentRepo = employmentRepo;
        this.housingRepo = housingRepo;
    }

    public void fetchEmploymentData() {
        log.info("Downloading employment data from StatsCan...");
        try {
            byte[] csvBytes = downloadAndExtractCsv(EMPLOYMENT_ZIP_URL, "14100382");
            parseEmploymentCsv(new InputStreamReader(new ByteArrayInputStream(csvBytes)));
            log.info("Employment data fetched and saved successfully.");
        } catch (Exception e) {
            log.error("Failed to fetch employment data from StatsCan", e);
            throw new RuntimeException("Employment data fetch failed", e);
        }
    }

    public void fetchHousingData() {
        log.info("Downloading housing data from StatsCan...");
        try {
            byte[] csvBytes = downloadAndExtractCsv(HOUSING_ZIP_URL, "34100143");
            parseHousingCsv(new InputStreamReader(new ByteArrayInputStream(csvBytes)));
            log.info("Housing data fetched and saved successfully.");
        } catch (Exception e) {
            log.error("Failed to fetch housing data from StatsCan", e);
            throw new RuntimeException("Housing data fetch failed", e);
        }
    }

    public String refreshData() {
        log.info("Refreshing all data...");
        try {
            List<Region> regions = regionRepo.findAll();
            for (Region r : regions) {
                employmentRepo.deleteByRegion(r);
                housingRepo.deleteByRegion(r);
            }
            fetchEmploymentData();
            fetchHousingData();
            log.info("Data refresh completed successfully.");
            return "Data refreshed successfully.";
        } catch (Exception e) {
            log.error("Data refresh failed", e);
            return "Data refresh failed: " + e.getMessage();
        }
    }

    private byte[] downloadAndExtractCsv(String zipUrl, String csvPrefix) throws IOException {
        URL url = new URL(zipUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(60_000);

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(conn.getInputStream()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith(csvPrefix) && name.endsWith(".csv") && !name.contains("MetaData")) {
                    log.info("Extracting CSV: {}", name);
                    return zis.readAllBytes();
                }
            }
        }
        throw new IOException("No matching CSV found in ZIP from " + zipUrl);
    }

    void parseEmploymentCsv(Reader reader) throws Exception {
        Map<String, Region> regionCache = getOrCreateRegions();
        List<EmploymentObservation> observations = new ArrayList<>();

        try (CSVReader csv = new CSVReaderBuilder(reader).build()) {
            String[] header = csv.readNext();
            int refDateIdx = findColumn(header, "REF_DATE");
            int geoIdx = findColumn(header, "GEO");
            int naicsIdx = findColumnContaining(header, "North American Industry");
            int valueIdx = findColumn(header, "VALUE");

            String[] row;
            while ((row = csv.readNext()) != null) {
                if (row.length <= valueIdx) continue;

                String geo = row[geoIdx];
                Region region = matchRegion(geo, regionCache);
                if (region == null) continue;

                String naics = row[naicsIdx].trim();
                if (!matchesBroadNaics(naics)) continue;

                String valueStr = row[valueIdx].trim();
                if (valueStr.isEmpty()) continue;

                EmploymentObservation obs = new EmploymentObservation();
                obs.setRegion(region);
                obs.setRefDate(row[refDateIdx].trim());
                obs.setNaicsIndustry(naics);
                obs.setValue(Double.parseDouble(valueStr));
                observations.add(obs);
            }
        }

        log.info("Parsed {} employment observations", observations.size());
        employmentRepo.saveAll(observations);
    }

    void parseHousingCsv(Reader reader) throws Exception {
        Map<String, Region> regionCache = getOrCreateRegions();
        List<HousingObservation> observations = new ArrayList<>();

        try (CSVReader csv = new CSVReaderBuilder(reader).build()) {
            String[] header = csv.readNext();
            int refDateIdx = findColumn(header, "REF_DATE");
            int geoIdx = findColumn(header, "GEO");
            int dwellingIdx = findColumnContaining(header, "Type of unit");
            int estimateIdx = findColumnContaining(header, "Housing estimates");
            int valueIdx = findColumn(header, "VALUE");

            String[] row;
            while ((row = csv.readNext()) != null) {
                if (row.length <= valueIdx) continue;

                String geo = row[geoIdx];
                Region region = matchRegion(geo, regionCache);
                if (region == null) continue;

                String dwellingRaw = row[dwellingIdx].trim();
                String dwellingType = mapDwellingType(dwellingRaw);
                if (dwellingType == null) continue;

                String estimateRaw = row[estimateIdx].trim();
                String metric = null;
                if (estimateRaw.toLowerCase().contains("starts")) metric = "Starts";
                else if (estimateRaw.toLowerCase().contains("completions")) metric = "Completions";
                if (metric == null) continue;

                String valueStr = row[valueIdx].trim();
                if (valueStr.isEmpty()) continue;

                HousingObservation obs = new HousingObservation();
                obs.setRegion(region);
                obs.setRefDate(row[refDateIdx].trim());
                obs.setDwellingType(dwellingType);
                obs.setMetric(metric);
                obs.setValue(Double.parseDouble(valueStr));
                observations.add(obs);
            }
        }

        log.info("Parsed {} housing observations", observations.size());
        housingRepo.saveAll(observations);
    }

    private Map<String, Region> getOrCreateRegions() {
        Map<String, Region> cache = new HashMap<>();
        Region toronto = regionRepo.findByCode("535")
                .orElseGet(() -> regionRepo.save(createRegion("Toronto", "535")));
        Region hamilton = regionRepo.findByCode("537")
                .orElseGet(() -> regionRepo.save(createRegion("Hamilton", "537")));
        cache.put("toronto", toronto);
        cache.put("hamilton", hamilton);
        return cache;
    }

    private Region createRegion(String name, String code) {
        Region r = new Region();
        r.setName(name);
        r.setCode(code);
        return r;
    }

    private Region matchRegion(String geo, Map<String, Region> cache) {
        if (geo == null) return null;
        String lower = geo.toLowerCase();
        if (lower.contains("toronto")) return cache.get("toronto");
        if (lower.contains("hamilton")) return cache.get("hamilton");
        return null;
    }

    private boolean matchesBroadNaics(String naics) {
        for (String broad : BROAD_NAICS) {
            if (naics.contains(broad)) return true;
        }
        return false;
    }

    private String mapDwellingType(String raw) {
        for (Map.Entry<String, String> e : DWELLING_TYPE_MAP.entrySet()) {
            if (raw.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    private int findColumn(String[] header, String name) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(name)) return i;
        }
        throw new IllegalArgumentException("Column not found: " + name);
    }

    private int findColumnContaining(String[] header, String substring) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].toLowerCase().contains(substring.toLowerCase())) return i;
        }
        throw new IllegalArgumentException("Column containing '" + substring + "' not found");
    }
}
