package org.viktor.syrkin;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class URLShortenerTest {
    private final List<String> testUrls = List.of(
            "https://example.com",
            "https://test.com/page1",
            "https://another-example.org/about"
    );

    @Test
    void testStrategiesUniqueness() {
        List<ShorteningStrategy> shorteningStrategies =
                List.of(new CounterStrategy(), new MD5Strategy(), new Base64Strategy());

        for (ShorteningStrategy strategy : shorteningStrategies) {
            URLShortener shortener = new URLShortener(strategy);

            Set<String> uniqueUrls = testUrls.stream()
                                             .map(shortener::shorten)
                                             .collect(Collectors.toSet());

            assertEquals(testUrls.size(), uniqueUrls.size(),
                    strategy.getClass() + " strategy should generate unique short URLs for different inputs");
        }
    }

    @RepeatedTest(10)
    void testRandomnessDistribution() {
        RandomStrategy strategy = new RandomStrategy();
        int sampleSize = 1000;
        Set<String> uniqueUrls = new HashSet<>();

        // Generate a large number of short URLs
        for (int i = 0; i < sampleSize; i++) {
            uniqueUrls.add(strategy.generateShortUrl("https://example.com"));
        }

        // With a good random distribution, we expect most URLs to be unique
        // The probability of collisions with 8 chars from 62 possible chars is very low
        double uniqueRatio = (double) uniqueUrls.size() / sampleSize;
        assertTrue(uniqueRatio > 0.99,
                "Expected at least 99% unique URLs, but got " + (uniqueRatio * 100) + "%");
    }

    @Test
    public void testBase64StrategyLengthConstraint() {
        URLShortener shortener = new URLShortener(new Base64Strategy());

        for (String longUrl : testUrls) {
            String shortUrl = shortener.shorten(longUrl);
            assertTrue(!shortUrl.isEmpty() && shortUrl.length() <= 8,
                    "Base64 strategy should generate non-empty short URLs with length <= 8");

            // Also verify that unshortening works correctly
            assertEquals(longUrl, shortener.unShorten(shortUrl),
                    "Should be able to unshorten the URL correctly");
        }
    }

    @Test
    public void testUnshortenCorrectness() {
        // Test with all strategies
        ShorteningStrategy[] strategies = {
                new CounterStrategy(),
                new MD5Strategy(),
                new Base64Strategy(),
                new RandomStrategy()
        };

        for (ShorteningStrategy strategy : strategies) {
            URLShortener shortener = new URLShortener(strategy);
            Map<String, String> map = new HashMap<>();

            for (String url : testUrls) {
                String shortUrl = shortener.shorten(url);
                map.put(shortUrl, url);
            }

            for (Map.Entry<String, String> entry : map.entrySet()) {
                assertEquals(entry.getValue(), shortener.unShorten(entry.getKey()),
                        "Unshortening should return the original URL for " + strategy.getClass().getSimpleName());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-url",
            "://example.com"
    })
    public void testInvalidUrlThrows(String invalidUrl) {
        URLShortener shortener = new URLShortener(new CounterStrategy());

        assertThrows(IllegalArgumentException.class, () -> {
            shortener.shorten(invalidUrl);
        }, "Should throw IllegalArgumentException for invalid URL: " + invalidUrl);
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testNullOrEmptyUrlThrows(String invalidUrl) {
        URLShortener shortener = new URLShortener(new CounterStrategy());

        assertThrows(IllegalArgumentException.class, () -> {
            shortener.shorten(invalidUrl);
        }, "Should throw IllegalArgumentException for null or empty URL");
    }

    @Test
    public void testUnshortenNonExistentUrl() {
        URLShortener shortener = new URLShortener(new CounterStrategy());

        // Try to unshorten a URL that doesn't exist
        String nonExistentShortUrl = "nonexistent";
        assertNull(shortener.unShorten(nonExistentShortUrl),
                "Unshortening a non-existent URL should return null");
    }

    @Test
    public void testIdempotence() {
        URLShortener shortener = new URLShortener(new CounterStrategy());

        // Shorten the same URL multiple times
        String url = "https://example.com";
        String firstShortUrl = shortener.shorten(url);

        // Subsequent calls should return the same short URL
        for (int i = 0; i < 10; i++) {
            assertEquals(firstShortUrl, shortener.shorten(url),
                    "Shortening the same URL multiple times should return the same short URL");
        }
    }
}