package com.example.brandinsightsapi.controller;

import com.example.brandinsightsapi.dto.BrandInfoRequest;
import com.example.brandinsightsapi.dto.BrandInfoResponse;
import com.example.brandinsightsapi.service.BrandInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // Root path for the API
public class BrandInfoController {

    private final BrandInfoService brandInfoService;

    @Autowired
    public BrandInfoController(BrandInfoService brandInfoService) {
        this.brandInfoService = brandInfoService;
    }

    @PostMapping("/extract-brand-info")
    public ResponseEntity<BrandInfoResponse> getBrandInformation(@RequestBody BrandInfoRequest request) {
        if (request == null || request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Or a custom error response
        }
        // Basic URL validation could be added here (e.g. regex)

        BrandInfoResponse response = brandInfoService.extractBrandInfo(request.getUrl());
        return ResponseEntity.ok(response);
    }
}
