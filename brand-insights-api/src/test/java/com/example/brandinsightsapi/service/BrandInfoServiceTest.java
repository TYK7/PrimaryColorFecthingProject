package com.example.brandinsightsapi.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;


import java.util.List;
import java.util.Set;
import java.util.HashSet;
// import java.util.Arrays; // Not directly used in this version of tests

import static org.junit.jupiter.api.Assertions.*;

class BrandInfoServiceTest {

    private BrandInfoService brandInfoService;

    @BeforeEach
    void setUp() {
        brandInfoService = new BrandInfoService();
    }

    // Helper to invoke private methods using reflection
    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            java.lang.reflect.Method method = BrandInfoService.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (T) method.invoke(brandInfoService, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method " + methodName, e);
        }
    }

    @Test
    void testExtractBrandName_ogSiteName() {
        Document doc = Jsoup.parse("<html><head><meta property=\"og:site_name\" content=\"OG Site Name\"><title>Page Title</title></head></html>");
        assertEquals("OG Site Name", invokePrivateMethod("extractBrandName", new Class[]{Document.class}, new Object[]{doc}));
    }
    @Test
    void testExtractBrandName_appName() {
        Document doc = Jsoup.parse("<html><head><meta name=\"application-name\" content=\"App Name\"><title>Page Title</title></head></html>");
        assertEquals("App Name", invokePrivateMethod("extractBrandName", new Class[]{Document.class}, new Object[]{doc}));
    }
    @Test
    void testExtractBrandName_ogTitle() {
        Document doc = Jsoup.parse("<html><head><meta property=\"og:title\" content=\"OG Title\"><title>Page Title</title></head></html>");
        assertEquals("OG Title", invokePrivateMethod("extractBrandName", new Class[]{Document.class}, new Object[]{doc}));
    }
    @Test
    void testExtractBrandName_titleTag() {
        Document doc = Jsoup.parse("<html><head><title>Page Title Only</title></head></html>");
        assertEquals("Page Title Only", invokePrivateMethod("extractBrandName", new Class[]{Document.class}, new Object[]{doc}));
    }
    @Test
    void testExtractBrandName_priority() {
        Document doc = Jsoup.parse("<html><head><meta property=\"og:site_name\" content=\"OG Site\"><meta name=\"application-name\" content=\"App Name\"><title>Title</title></head></html>");
        assertEquals("OG Site", invokePrivateMethod("extractBrandName", new Class[]{Document.class}, new Object[]{doc}));
    }

    @Test
    void testExtractLogoUrl_ogImage() {
        Document doc = Jsoup.parse("<html><head><meta property=\"og:image\" content=\"http://example.com/og_logo.png\"></head></html>", "http://example.com");
        assertEquals("http://example.com/og_logo.png", invokePrivateMethod("extractLogoUrl", new Class[]{Document.class}, new Object[]{doc}));
    }
    @Test
    void testExtractLogoUrl_imgIdLogo() {
        Document doc = Jsoup.parse("<html><body><img id=\"logo\" src=\"logo_id.png\"></body></html>", "http://example.com");
        assertEquals("http://example.com/logo_id.png", invokePrivateMethod("extractLogoUrl", new Class[]{Document.class}, new Object[]{doc}));
    }
     @Test
    void testExtractLogoUrl_relativeToAbsolute() {
        Document doc = Jsoup.parse("<html><head><meta property=\"og:image\" content=\"/images/og_logo.png\"></head></html>", "http://example.com/page/");
        assertEquals("http://example.com/images/og_logo.png", invokePrivateMethod("extractLogoUrl", new Class[]{Document.class}, new Object[]{doc}));
    }

    @ParameterizedTest
    @CsvSource({
            "'#abc', '#AABBCC'",
            "'#ABC', '#AABBCC'",
            "'#aabbcc', '#AABBCC'",
            "'#AABBCC', '#AABBCC'",
            "'rgb(255,0,0)', '#FF0000'",
            "'rgb( 255 , 0 , 0 )', '#FF0000'",
            "'rgba(0,255,0,1)', '#00FF00'",
            "'rgba(0, 0, 255, 0.5)', '#0000FF'", // Alpha ignored
            "'red', '#FF0000'", // Requires NamedColorConverter to be populated
            "'WHITE', '#FFFFFF'", // Requires NamedColorConverter to be populated
            "'invalidcolor', null",
            "'#12345', null",
            "'#1234567', null"
    })
    void testNormalizeColorToHex_variousFormats(String input, String expected) {
        // Note: NamedColorConverter needs to be populated for 'red' and 'WHITE' to pass.
        // The actual NamedColorConverter in BrandInfoService has more entries.
        assertEquals(expected, invokePrivateMethod("normalizeColorToHex", new Class[]{String.class}, new Object[]{input}));
    }

    @Test
    void testParseFontFamiliesFromText_singleFont() {
        Set<String> fonts = new HashSet<>();
        invokePrivateMethod("parseFontFamiliesFromText", new Class[]{String.class, Set.class}, new Object[]{"font-family: Arial;", fonts});
        assertTrue(fonts.contains("Arial"));
    }
    @Test
    void testParseFontFamiliesFromText_multipleFonts() {
        Set<String> fonts = new HashSet<>();
        invokePrivateMethod("parseFontFamiliesFromText", new Class[]{String.class, Set.class}, new Object[]{"font-family: 'Times New Roman', serif, 'Courier New';", fonts});
        assertTrue(fonts.contains("Times New Roman"));
        assertFalse(fonts.contains("serif"));
        assertTrue(fonts.contains("Courier New"));
    }
    @Test
    void testParseFontFamiliesFromText_quotedFonts() {
        Set<String> fonts = new HashSet<>();
        invokePrivateMethod("parseFontFamiliesFromText", new Class[]{String.class, Set.class}, new Object[]{"font-family: \"My Custom Font\", sans-serif;", fonts});
        assertTrue(fonts.contains("My Custom Font"));
        assertFalse(fonts.contains("sans-serif"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"serif", "sans-serif", "monospace", "cursive", "fantasy", "system-ui"})
    void testIsGenericFontFamily_isGeneric(String fontName) {
        assertTrue((Boolean) invokePrivateMethod("isGenericFontFamily", new Class[]{String.class}, new Object[]{fontName}));
    }
    @ParameterizedTest
    @ValueSource(strings = {"Arial", "Roboto", "MyFont"})
    void testIsGenericFontFamily_notGeneric(String fontName) {
        assertFalse((Boolean) invokePrivateMethod("isGenericFontFamily", new Class[]{String.class}, new Object[]{fontName}));
    }

    @Test
    void testExtractAllImageUrls_basicImgTags() {
        Document doc = Jsoup.parse("<html><body><img src=\"img1.png\"><img src=\"http://example.com/img2.jpg\"></body></html>", "http://example.com/");
        List<String> urls = invokePrivateMethod("extractAllImageUrls", new Class[]{Document.class}, new Object[]{doc});
        assertEquals(2, urls.size());
        assertTrue(urls.contains("http://example.com/img1.png"));
        assertTrue(urls.contains("http://example.com/img2.jpg"));
    }
    @Test
    void testExtractAllImageUrls_pictureSourceTags() {
        Document doc = Jsoup.parse("<html><body><picture><source srcset=\"img_small.jpg 1x, img_large.jpg 2x\"><img src=\"fallback.png\"></picture></body></html>", "http://example.com/");
        List<String> urls = invokePrivateMethod("extractAllImageUrls", new Class[]{Document.class}, new Object[]{doc});
        assertTrue(urls.contains("http://example.com/img_small.jpg"));
        assertTrue(urls.contains("http://example.com/img_large.jpg"));
        assertTrue(urls.contains("http://example.com/fallback.png"));
    }
    @Test
    void testExtractAllImageUrls_backgroundImageStyle() {
        Document doc = Jsoup.parse("<html><body><div style=\"background-image: url('bg.gif');\"></div><span style=\"background:url(http://othersite.com/bg2.webp)\"></span></body></html>", "http://example.com/");
        List<String> urls = invokePrivateMethod("extractAllImageUrls", new Class[]{Document.class}, new Object[]{doc});
        assertTrue(urls.contains("http://example.com/bg.gif"));
        assertTrue(urls.contains("http://othersite.com/bg2.webp"));
    }

    @ParameterizedTest
    @CsvSource({
            "'http://example.com/image.png', true",
            "'https://example.com/image.jpg', true",
            "'//example.com/image.gif', false",
            "'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA', false",
            "'ftp://example.com/image.png', false",
            "NIL_VALUE, false", // Represents null
            "'', false",
            "'http://example.com/page', true"
    })
    void testIsValidImageUrl_variousCases(String input, boolean expected) {
        if ("NIL_VALUE".equals(input)) input = null;
        assertEquals(expected, (Boolean) invokePrivateMethod("isValidImageUrl", new Class[]{String.class}, new Object[]{input}));
    }
}
