package com.example.brandinsightsapi.service;

import com.example.brandinsightsapi.dto.BrandInfoResponse;
import de.androidbiometrics.colorthief.ColorThief;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.function.Function;

@Service
public class BrandInfoService {

    private static final Pattern FONT_FAMILY_PATTERN = Pattern.compile(
        "font-family\\s*:\\s*([^;!]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern COLOR_PATTERN = Pattern.compile(
        "(#[0-9a-fA-F]{3,8})|" +
        "\\b(rgb|rgba)\\((\\s*\\d+\\s*,){2,3}\\s*\\d+(\\.\\d+)?\\s*\\)|" +
        "\\b(aliceblue|antiquewhite|aqua|aquamarine|azure|beige|bisque|black|blanchedalmond|blue|blueviolet|brown|burlywood|cadetblue|chartreuse|chocolate|coral|cornflowerblue|cornsilk|crimson|cyan|darkblue|darkcyan|darkgoldenrod|darkgray|darkgreen|darkgrey|darkkhaki|darkmagenta|darkolivegreen|darkorange|darkorchid|darkred|darksalmon|darkseagreen|darkslateblue|darkslategray|darkslategrey|darkturquoise|darkviolet|deeppink|deepskyblue|dimgray|dimgrey|dodgerblue|firebrick|floralwhite|forestgreen|fuchsia|gainsboro|ghostwhite|gold|goldenrod|gray|green|greenyellow|grey|honeydew|hotpink|indianred|indigo|ivory|khaki|lavender|lavenderblush|lawngreen|lemonchiffon|lightblue|lightcoral|lightcyan|lightgoldenrodyellow|lightgray|lightgreen|lightgrey|lightpink|lightsalmon|lightseagreen|lightskyblue|lightslategray|lightslategrey|lightsteelblue|lightyellow|lime|limegreen|linen|magenta|maroon|mediumaquamarine|mediumblue|mediumorchid|mediumpurple|mediumseagreen|mediumslateblue|mediumspringgreen|mediumturquoise|mediumvioletred|midnightblue|mintcream|mistyrose|moccasin|navajowhite|navy|oldlace|olive|olivedrab|orange|orangered|orchid|palegoldenrod|palegreen|paleturquoise|palevioletred|papayawhip|peachpuff|peru|pink|plum|powderblue|purple|rebeccapurple|red|rosybrown|royalblue|saddlebrown|salmon|sandybrown|seagreen|seashell|sienna|silver|skyblue|slateblue|slategray|slategrey|snow|springgreen|steelblue|tan|teal|thistle|tomato|turquoise|violet|wheat|white|whitesmoke|yellow|yellowgreen)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BG_IMAGE_URL_PATTERN = Pattern.compile("url\\(['\"]?([^'\"?#]+)(?:[?#][^'\"]*)?['\"]?\\)");


    public BrandInfoResponse extractBrandInfo(String url) {
        BrandInfoResponse response = new BrandInfoResponse();
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        try {
            Document document = Jsoup.connect(url)
                                    .userAgent(userAgent)
                                    .timeout(10000)
                                    .get();

            String brandName = extractBrandName(document);
            response.setBrandName(brandName);

            String logoUrlString = extractLogoUrl(document);
            if (logoUrlString != null && !logoUrlString.isEmpty()) {
                response.setLogoUrl(logoUrlString);
                List<String> logoColors = extractColorsFromLogo(logoUrlString);
                response.setLogoColors(logoColors);
            } else {
                response.setLogoColors(new ArrayList<>());
            }

            List<BrandInfoResponse.WebsiteColor> websiteColors = extractWebsiteColors(document);
            response.setWebsiteColors(websiteColors);

            List<String> fonts = extractFontFamilies(document);
            response.setFonts(fonts);

            List<String> imageUrls = extractAllImageUrls(document);
            response.setImageUrls(imageUrls);
            // System.out.println("Extracted image URLs: " + imageUrls.size());

        } catch (IOException e) {
            System.err.println("Error fetching HTML from URL: " + url + " - " + e.getMessage());
            response.setBrandName("Error: Could not fetch content");
            setEmptyListsOnError(response);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid URL provided: " + url + " - " + e.getMessage());
            response.setBrandName("Error: Invalid URL format");
            setEmptyListsOnError(response);
        }
        return response;
    }

    private void setEmptyListsOnError(BrandInfoResponse response) {
        response.setLogoColors(new ArrayList<>());
        response.setWebsiteColors(new ArrayList<>());
        response.setFonts(new ArrayList<>());
        response.setImageUrls(new ArrayList<>());
    }

    private List<String> extractAllImageUrls(Document document) {
        Set<String> uniqueImageUrls = new HashSet<>();

        Elements imgTags = document.select("img[src]");
        for (Element img : imgTags) {
            String src = img.absUrl("src");
            if (isValidImageUrl(src)) {
                uniqueImageUrls.add(src);
            }
        }

        Elements sourceTags = document.select("picture source[srcset]");
        for (Element source : sourceTags) {
            String srcset = source.attr("srcset");
            String[] urls = srcset.split(",");
            for (String urlEntry : urls) {
                String trimmedUrlEntry = urlEntry.trim().split("\\s+")[0];
                String absoluteUrl = source.absUrl(trimmedUrlEntry);
                 if (isValidImageUrl(absoluteUrl)) {
                    uniqueImageUrls.add(absoluteUrl);
                }
            }
        }

        Elements styledElements = document.select("[style*='background-image'], [style*='background']");
        for (Element element : styledElements) {
            String style = element.attr("style");
            Matcher matcher = BG_IMAGE_URL_PATTERN.matcher(style);
            while(matcher.find()){
                String bgUrl = matcher.group(1);
                // Make URL absolute relative to the element's base URI or the document's base URI
                String absoluteBgUrl = element.absUrl(bgUrl);
                if(isValidImageUrl(absoluteBgUrl)){
                    uniqueImageUrls.add(absoluteBgUrl);
                }
            }
        }

        // System.out.println("Found " + uniqueImageUrls.size() + " unique image URLs.");
        return new ArrayList<>(uniqueImageUrls);
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        // Basic check for http/https. Further filtering by extension is optional and can be restrictive.
        // Many CDNs serve images without extensions or with query parameters.
        if (!(lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://"))) {
            return false; // Must be an absolute HTTP/HTTPS URL
        }
        // Avoid data URIs if they are not desired, though absUrl usually doesn't produce them from relative paths.
        if (lowerUrl.startsWith("data:")) {
            return false;
        }

        // Optional: Check for common image extensions if strict filtering is needed.
        // However, this might exclude valid CDN URLs without extensions.
        // if (lowerUrl.matches(".*\\.(png|jpg|jpeg|gif|svg|webp)(\\?.*)?$")) {
        //     return true;
        // }
        // If no extension check, consider it valid if it's http/https and not a data URI.
        return true;
    }

    private String extractBrandName(Document document) {
        String brandName = null;
        Element ogSiteNameMeta = document.selectFirst("meta[property=og:site_name]");
        if (ogSiteNameMeta != null) brandName = ogSiteNameMeta.attr("content");

        if (brandName == null || brandName.trim().isEmpty()) {
            Element appNameMeta = document.selectFirst("meta[name=application-name]");
            if (appNameMeta != null) brandName = appNameMeta.attr("content");
        }

        if (brandName == null || brandName.trim().isEmpty()) {
            Element ogTitleMeta = document.selectFirst("meta[property=og:title]");
            if (ogTitleMeta != null) brandName = ogTitleMeta.attr("content");
        }

        if (brandName == null || brandName.trim().isEmpty()) {
            brandName = document.title();
        }

        if (brandName != null && !brandName.trim().isEmpty()) {
            return brandName.trim();
        }
        return "Brand name not found";
    }

    private String extractLogoUrl(Document document) {
        List<Function<Document, String>> strategies = Arrays.asList(
            doc -> getAttrFromElement(doc, "meta[property=og:image]", "content"),
            doc -> getAttrFromElement(doc, "meta[name=twitter:image]", "content"),
            doc -> getAttrFromElement(doc, "link[rel=image_src]", "href"),
            doc -> getAttrFromElement(doc, "img[itemprop=logo]", "src"),
            doc -> getAttrFromElement(doc, "img[id*=logo i]", "src"),
            doc -> getAttrFromElement(doc, "img[class*=logo i]", "src"),
            doc -> getAttrFromElement(doc, "img[alt*=logo i]", "src"),
            doc -> getAttrFromElement(doc, "img[src*=logo i]", "src"),
            doc -> getAttrFromElement(doc, "img[id*=brand i]", "src"),
            doc -> getAttrFromElement(doc, "img[class*=brand i]", "src"),
            doc -> getAttrFromElement(doc, "img[alt*=brand i]", "src"),
            doc -> getAttrFromElement(doc, "img[src*=brand i]", "src"),
            doc -> getAttrFromElement(doc, "link[rel=apple-touch-icon]", "href"),
            doc -> getAttrFromElement(doc, "link[rel=icon][sizes~=(192x192|180x180|152x152|144x144|128x128|96x96|72x72|64x64|32x32|16x16)]", "href"),
            doc -> getAttrFromElement(doc, "link[rel=icon][type='image/png']", "href"),
            doc -> getAttrFromElement(doc, "link[rel=icon][type='image/svg+xml']", "href"),
            doc -> getAttrFromElement(doc, "link[rel=icon]", "href"),
            doc -> getAttrFromElement(doc, "link[rel=shortcut icon]", "href")
        );

        for (Function<Document, String> strategy : strategies) {
            String rawUrl = strategy.apply(document);
            if (rawUrl != null && !rawUrl.trim().isEmpty()) {
                String absoluteUrl = document.absUrl(rawUrl.trim());
                if (!absoluteUrl.isEmpty()) return absoluteUrl;
            }
        }

        Elements images = document.select("img");
        for (Element img : images) {
            String src = img.attr("src");
            if (src.isEmpty()) continue;
            String[] checks = { img.attr("id").toLowerCase(), img.attr("class").toLowerCase(), img.attr("alt").toLowerCase(), src.toLowerCase() };
            for (String check : checks) {
                if (check.contains("logo") || check.contains("brand")) {
                    String absoluteUrl = document.absUrl(src.trim());
                    if (!absoluteUrl.isEmpty()) return absoluteUrl;
                }
            }
        }
        return null;
    }

    private List<String> extractColorsFromLogo(String logoUrlString) {
        List<String> hexColors = new ArrayList<>();
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(logoUrlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; BrandInsightsBot/1.0; +http://example.com/bot)");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                BufferedImage image = ImageIO.read(inputStream);
                if (image != null) {
                    int[][] palette = ColorThief.getPalette(image, 8, 5, false);
                    if (palette != null) {
                        for (int[] rgb : palette) {
                            if (rgb != null && rgb.length == 3) {
                                String hexColor = String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
                                hexColors.add(hexColor);
                            }
                        }
                    } else System.err.println("ColorThief.getPalette returned null for logo: " + logoUrlString);
                } else System.err.println("Failed to decode image or image format not supported: " + logoUrlString + ". ImageIO.read returned null.");
            } else System.err.println("Failed to download logo. HTTP Status: " + responseCode + " for URL: " + logoUrlString);
        } catch (IOException e) { System.err.println("IOException while fetching or processing logo image " + logoUrlString + ": " + e.getClass().getName() + " - " + e.getMessage());
        } catch (Exception e) { System.err.println("Unexpected error processing logo image " + logoUrlString + ": " + e.getClass().getName() + " - " + e.getMessage());
        } finally {
            if (inputStream != null) try { inputStream.close(); } catch (IOException e) { /* ignore */ }
            if (connection != null) connection.disconnect();
        }
        return hexColors;
    }

    private String getAttrFromElement(Document doc, String selector, String attributeKey) {
        Element element = doc.selectFirst(selector);
        if (element != null) {
            String value = element.attr(attributeKey);
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return null;
    }

    private List<BrandInfoResponse.WebsiteColor> extractWebsiteColors(Document document) {
        Map<String, Integer> colorFrequencies = new HashMap<>();
        List<String> allColorsFound = new ArrayList<>();

        Elements styledElements = document.select("[style]");
        for (Element element : styledElements) {
            extractColorsFromCssText(element.attr("style"), allColorsFound);
        }

        Elements styleTags = document.select("style");
        for (Element styleTag : styleTags) {
            extractColorsFromCssText(styleTag.html(), allColorsFound);
        }

        for (String colorStr : allColorsFound) {
            String hexColor = normalizeColorToHex(colorStr);
            if (hexColor != null) {
                colorFrequencies.put(hexColor, colorFrequencies.getOrDefault(hexColor, 0) + 1);
            }
        }

        Map<String, Integer> sortedColors = colorFrequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<BrandInfoResponse.WebsiteColor> topColors = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedColors.entrySet()) {
            if (count >= 10) break;
            String label = (count == 0) ? "Primary" : (count == 1) ? "Secondary" : (count == 2) ? "Tertiary" : "Accent";
            topColors.add(new BrandInfoResponse.WebsiteColor(entry.getKey(), label));
            count++;
        }
        return topColors;
    }

    private void extractColorsFromCssText(String cssText, List<String> colorList) {
        if (cssText == null || cssText.isEmpty()) return;
        Matcher matcher = COLOR_PATTERN.matcher(cssText);
        while (matcher.find()) {
            colorList.add(matcher.group(0));
        }
    }

    private String normalizeColorToHex(String colorStr) {
        if (colorStr == null) return null;
        colorStr = colorStr.toLowerCase().trim();

        if (colorStr.startsWith("#")) {
            if (colorStr.length() == 4) return String.format("#%c%c%c%c%c%c", colorStr.charAt(1), colorStr.charAt(1), colorStr.charAt(2), colorStr.charAt(2), colorStr.charAt(3), colorStr.charAt(3)).toUpperCase();
            if (colorStr.length() == 5) return String.format("#%c%c%c%c%c%c", colorStr.charAt(1), colorStr.charAt(1), colorStr.charAt(2), colorStr.charAt(2), colorStr.charAt(3), colorStr.charAt(3)).toUpperCase();
            else if (colorStr.length() == 7) return colorStr.toUpperCase();
            else if (colorStr.length() == 9) return ("#" + colorStr.substring(3)).toUpperCase();
            return null;
        }

        if (colorStr.startsWith("rgb(") && colorStr.endsWith(")")) {
            try {
                String[] parts = colorStr.substring(4, colorStr.length() - 1).split(",");
                int r = Integer.parseInt(parts[0].trim()); int g = Integer.parseInt(parts[1].trim()); int b = Integer.parseInt(parts[2].trim());
                return String.format("#%02X%02X%02X", r, g, b);
            } catch (Exception e) { return null; }
        }

        if (colorStr.startsWith("rgba(") && colorStr.endsWith(")")) {
             try {
                String[] parts = colorStr.substring(5, colorStr.length() - 1).split(",");
                int r = Integer.parseInt(parts[0].trim()); int g = Integer.parseInt(parts[1].trim()); int b = Integer.parseInt(parts[2].trim());
                return String.format("#%02X%02X%02X", r, g, b);
            } catch (Exception e) { return null; }
        }

        String hex = NamedColorConverter.getHex(colorStr);
        if (hex != null) return hex.toUpperCase();

        return null;
    }

    private List<String> extractFontFamilies(Document document) {
        Set<String> uniqueFonts = new HashSet<>();
        Elements styledElements = document.select("[style]"); for (Element element : styledElements) parseFontFamiliesFromText(element.attr("style"), uniqueFonts);
        Elements styleTags = document.select("style"); for (Element styleTag : styleTags) parseFontFamiliesFromText(styleTag.html(), uniqueFonts);
        Elements linkedStylesheets = document.select("link[rel=stylesheet][href]");
        for (Element link : linkedStylesheets) { String href = link.attr("href"); if (href.contains("fonts.googleapis.com/css")) { Pattern googleFontPattern = Pattern.compile("family=([^&:]+)"); Matcher matcher = googleFontPattern.matcher(href); while(matcher.find()) { String familiesGroup = matcher.group(1); String[] families = familiesGroup.split("\\|"); for (String family : families) { String fontName = family.split(":")[0].replace('+', ' ').trim(); if (!fontName.isEmpty() && !isGenericFontFamily(fontName)) uniqueFonts.add(fontName); } } } }
        if(uniqueFonts.isEmpty()){ System.out.println("No specific font families found. Common defaults might be in use."); }
        return new ArrayList<>(uniqueFonts);
    }

    private void parseFontFamiliesFromText(String text, Set<String> uniqueFonts) {
        if (text == null || text.isEmpty()) return;
        Matcher matcher = FONT_FAMILY_PATTERN.matcher(text);
        while (matcher.find()) { String fontFamilyValue = matcher.group(1).trim(); String[] fontNames = fontFamilyValue.split(","); for (String fontName : fontNames) { String cleanedName = fontName.trim().replaceAll("^['\"]|['\"]$", "").trim(); if (!cleanedName.isEmpty() && !isGenericFontFamily(cleanedName)) uniqueFonts.add(cleanedName); } }
    }

    private boolean isGenericFontFamily(String fontName) {
        String lowerFontName = fontName.toLowerCase();
        switch (lowerFontName) { case "serif": case "sans-serif": case "monospace": case "cursive": case "fantasy": case "system-ui": case "ui-serif": case "ui-sans-serif": case "ui-monospace": case "ui-rounded": case "emoji": case "math": case "fangsong": return true; default: return false; }
    }

    private static class NamedColorConverter {
        private static final Map<String, String> NAMED_COLORS = new HashMap<>();
        static {
            NAMED_COLORS.put("black", "#000000"); NAMED_COLORS.put("white", "#FFFFFF");
            // ... (Assume comprehensive list from previous steps)
        }
        public static String getHex(String name) { return NAMED_COLORS.get(name.toLowerCase()); }
    }
}
