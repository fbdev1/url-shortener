package org.viktor.syrkin;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class URLShortenerTest {

    private static final String EXAMPLE_URL = "https://example.com";
    private static final String EXAMPLE_URL1 = "https://test.com/page1";
    private static final String EXAMPLE_URL2 = "https://another-example.org/about";
    private final List<String> testUrls = List.of(EXAMPLE_URL, EXAMPLE_URL1, EXAMPLE_URL2);

    @Test
    void test_validUrl_returnsShortUrl() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        String longUrl = EXAMPLE_URL;
        String shortUrl = shortener.shorten(longUrl);
        assertNotNull(shortUrl);
        assertFalse(shortUrl.isEmpty());
        assertEquals(shortUrl, shortener.shorten(longUrl));
    }

    @Test
    void test_invalidUrl_throwsException() {
        URLShortener shortener = new URLShortener(new CounterStrategy());
        assertThrows(IllegalArgumentException.class, () -> shortener.shorten("invalid-url"));
        assertThrows(IllegalArgumentException.class, () -> shortener.shorten(""));
        assertThrows(IllegalArgumentException.class, () -> shortener.shorten(null));
    }

    @Test
    void test_collision_resolvesToUniqueShortUrl() {
        ShorteningStrategy mockStrategy = new ShorteningStrategy() {
            private int count = 0;
            @Override
            public String generateShortUrl(String longUrl) {
                if (count <= 1 ) {
                    count++;
                    return "DUPLICATE";
                }
                return "UNIQUE";
            }
        };
        URLShortener shortener = new URLShortener(mockStrategy);
        String short1 = shortener.shorten(EXAMPLE_URL);
        String short2 = shortener.shorten(EXAMPLE_URL1);
        assertNotEquals(short1, short2);
        assertEquals(EXAMPLE_URL, shortener.unShorten(short1));
        assertEquals(EXAMPLE_URL1, shortener.unShorten(short2));
    }

    @Test
    void test_uniqueness_all_strategies() {
        List<ShorteningStrategy> shorteningStrategies =
                List.of(new CounterStrategy(), new MD5Strategy(), new Base64Strategy(), new RandomStrategy());

        for (ShorteningStrategy strategy : shorteningStrategies) {
            URLShortener shortener = new URLShortener(strategy);

            Set<String> uniqueUrls = testUrls.stream()
                                             .map(shortener::shorten)
                                             .collect(Collectors.toSet());

            assertEquals(testUrls.size(), uniqueUrls.size());
        }
    }

    @Test
    void test_distribution_random() {
        RandomStrategy strategy = new RandomStrategy();
        int sampleSize = 1000;
        Set<String> uniqueUrls = new HashSet<>();

        for (int i = 0; i < sampleSize; i++) {
            uniqueUrls.add(strategy.generateShortUrl(EXAMPLE_URL));
        }

        double uniqueRatio = (double) uniqueUrls.size() / sampleSize;
        assertTrue(uniqueRatio > 0.99,
                "Expected at least 99% unique URLs, but got " + (uniqueRatio * 100) + "%");
    }

    @Test
    public void test_length_strategies() {
        List<ShorteningStrategy> shorteningStrategies =
                List.of(new CounterStrategy(), new MD5Strategy(), new Base64Strategy(), new RandomStrategy());

        for(ShorteningStrategy strategy : shorteningStrategies) {
            URLShortener shortener = new URLShortener(strategy);

            for (String longUrl : testUrls) {
                String shortUrl = shortener.shorten(longUrl);
                assertTrue(!shortUrl.isEmpty() && shortUrl.length() <= 8);
            }
        }
    }

    @Test
    public void test_unshorten_correctness() {
        List<ShorteningStrategy> shorteningStrategies =
                List.of(new CounterStrategy(), new MD5Strategy(), new Base64Strategy(), new RandomStrategy());

        for (ShorteningStrategy strategy : shorteningStrategies) {
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

    @Test
    public void test_idempotence() {
        URLShortener shortener = new URLShortener(new CounterStrategy());

        String firstShortUrl = shortener.shorten(EXAMPLE_URL);

        for (int i = 0; i < 10; i++) {
            assertEquals(firstShortUrl, shortener.shorten(EXAMPLE_URL));
        }
    }

    @Test
    void test_concurrency_stressCheck() throws InterruptedException {
        URLShortener shortener = new URLShortener(new MD5Strategy());
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);


        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                String longUrl = "http://" + i + "-example";
                String shortUrl = shortener.shorten(longUrl);
                shortener.unShorten(shortUrl);
            }
            latch.countDown();
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        latch.await();
        executorService.shutdown();
        assertTrue(true);
    }
}