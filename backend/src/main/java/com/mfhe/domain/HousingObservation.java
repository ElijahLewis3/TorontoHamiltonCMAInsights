package com.mfhe.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "housing_observations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"region_id", "ref_date", "dwelling_type", "metric"}))
public class HousingObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "ref_date", nullable = false)
    private String refDate;

    @Column(name = "dwelling_type", nullable = false)
    private String dwellingType;

    @Column(nullable = false)
    private String metric;

    private Double value;

    public HousingObservation() {
    }

    public HousingObservation(Region region, String refDate, String dwellingType, String metric, Double value) {
        this.region = region;
        this.refDate = refDate;
        this.dwellingType = dwellingType;
        this.metric = metric;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getRefDate() {
        return refDate;
    }

    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    public String getDwellingType() {
        return dwellingType;
    }

    public void setDwellingType(String dwellingType) {
        this.dwellingType = dwellingType;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
