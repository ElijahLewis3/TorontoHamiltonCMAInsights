package com.mfhe.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "employment_observations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"region_id", "ref_date", "naics_industry"}))
public class EmploymentObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "ref_date", nullable = false)
    private String refDate;

    @Column(name = "naics_industry", nullable = false)
    private String naicsIndustry;

    private Double value;

    public EmploymentObservation() {
    }

    public EmploymentObservation(Region region, String refDate, String naicsIndustry, Double value) {
        this.region = region;
        this.refDate = refDate;
        this.naicsIndustry = naicsIndustry;
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

    public String getNaicsIndustry() {
        return naicsIndustry;
    }

    public void setNaicsIndustry(String naicsIndustry) {
        this.naicsIndustry = naicsIndustry;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
