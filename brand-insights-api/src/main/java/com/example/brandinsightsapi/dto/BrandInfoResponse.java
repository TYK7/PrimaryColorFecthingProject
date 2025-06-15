package com.example.brandinsightsapi.dto;

import java.util.List;

public class BrandInfoResponse {
    private String brandName;
    private String logoUrl;
    private List<String> logoColors;
    private List<WebsiteColor> websiteColors;
    private List<String> fonts;
    private List<String> imageUrls;

    // Inner class/record for WebsiteColor
    public static class WebsiteColor {
        private String colorHex;
        private String label; // e.g., "primary", "secondary"

        public WebsiteColor(String colorHex, String label) {
            this.colorHex = colorHex;
            this.label = label;
        }

        public String getColorHex() {
            return colorHex;
        }

        public void setColorHex(String colorHex) {
            this.colorHex = colorHex;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    // Getters and Setters for BrandInfoResponse fields
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public List<String> getLogoColors() { return logoColors; }
    public void setLogoColors(List<String> logoColors) { this.logoColors = logoColors; }
    public List<WebsiteColor> getWebsiteColors() { return websiteColors; }
    public void setWebsiteColors(List<WebsiteColor> websiteColors) { this.websiteColors = websiteColors; }
    public List<String> getFonts() { return fonts; }
    public void setFonts(List<String> fonts) { this.fonts = fonts; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
