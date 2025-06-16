package com.example.brandinsightsapi.dto;

import java.util.List;
import java.util.ArrayList; // Added for initializing lists

public class BrandInfoResponse {
    private boolean success;
    private String errorMessage;

    private String brandName;
    private String logoUrl;
    private List<String> logoColors = new ArrayList<>(); // Initialize to prevent nulls
    private List<WebsiteColor> websiteColors = new ArrayList<>(); // Initialize
    private List<String> fonts = new ArrayList<>(); // Initialize
    private List<String> imageUrls = new ArrayList<>(); // Initialize

    // Inner class/record for WebsiteColor
    public static class WebsiteColor {
        private String colorHex;
        private String label; // e.g., "primary", "secondary"

        public WebsiteColor(String colorHex, String label) {
            this.colorHex = colorHex;
            this.label = label;
        }

        public String getColorHex() { return colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public List<String> getLogoColors() { return logoColors; }
    public void setLogoColors(List<String> logoColors) { this.logoColors = logoColors != null ? logoColors : new ArrayList<>(); }
    public List<WebsiteColor> getWebsiteColors() { return websiteColors; }
    public void setWebsiteColors(List<WebsiteColor> websiteColors) { this.websiteColors = websiteColors != null ? websiteColors : new ArrayList<>(); }
    public List<String> getFonts() { return fonts; }
    public void setFonts(List<String> fonts) { this.fonts = fonts != null ? fonts : new ArrayList<>(); }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>(); }
}
