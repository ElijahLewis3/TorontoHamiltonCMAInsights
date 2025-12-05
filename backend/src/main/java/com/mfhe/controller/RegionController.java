package com.mfhe.controller;

import com.mfhe.dto.RegionDto;
import com.mfhe.repository.RegionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    private final RegionRepository regionRepo;

    public RegionController(RegionRepository regionRepo) {
        this.regionRepo = regionRepo;
    }

    @GetMapping
    public List<RegionDto> getAllRegions() {
        return regionRepo.findAll().stream()
                .map(r -> new RegionDto(r.getId(), r.getName(), r.getCode()))
                .toList();
    }
}
