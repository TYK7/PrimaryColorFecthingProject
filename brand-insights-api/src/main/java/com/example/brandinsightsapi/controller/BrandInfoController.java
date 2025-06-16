package com.example.brandinsightsapi.controller;

import com.example.brandinsightsapi.dto.BrandInfoRequest;
import com.example.brandinsightsapi.dto.BrandInfoResponse;
import com.example.brandinsightsapi.service.BrandInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Added
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BrandInfoController {

    private final BrandInfoService brandInfoService;

    @Autowired
    public BrandInfoController(BrandInfoService brandInfoService) {
        this.brandInfoService = brandInfoService;
    }

    @PostMapping("/extract-brand-info")
    public ResponseEntity<BrandInfoResponse> getBrandInformation(@RequestBody BrandInfoRequest request) {
        if (request == null || request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            BrandInfoResponse errorResponse = new BrandInfoResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Request body or URL must not be empty.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        BrandInfoResponse response = brandInfoService.extractBrandInfo(request.getUrl());

        if (!response.isSuccess()) {
            String errorMessage = response.getErrorMessage() != null ? response.getErrorMessage().toLowerCase() : "";
            if (errorMessage.contains("invalid url format") ||
                errorMessage.contains("could not reach host") ||
                errorMessage.contains("timed out")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // For other content processing errors where URL might be valid but unprocessable by the service
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
