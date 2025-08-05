package org.viktor.syrkin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class URLShortener {

    private final ConcurrentHashMap<String, String> urlToShort = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> shortToUrl = new ConcurrentHashMap<>();

    private final ShorteningStrategy strategy;

    public URLShortener(ShorteningStrategy strategy) {
        this.strategy = strategy;
    }

    public String shorten(String longUrl) {
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        // Fast path: already shortened
        String existing = urlToShort.get(longUrl);
        if (existing != null) {
            return existing;
        }

        // Generate new short URL
        String shortUrl = strategy.generateShortUrl(longUrl);

        // Atomically insert if absent
        String prevShort = urlToShort.putIfAbsent(longUrl, shortUrl);
        if (prevShort != null) {
            return prevShort; // Another thread beat us
        }

        shortToUrl.put(shortUrl, longUrl); // This can safely go after
        return shortUrl;
    }

    public String unShorten(String shortUrl) {
        return shortToUrl.get(shortUrl);
    }

    private boolean isValidUrl(String url) {
        try {
            URL parsed = new URL(url);
            return !parsed.getHost().isEmpty();
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
