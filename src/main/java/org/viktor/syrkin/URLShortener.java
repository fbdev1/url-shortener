package org.viktor.syrkin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class URLShortener {

    private final ConcurrentHashMap<String, String> longToShort = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> shortToLong = new ConcurrentHashMap<>();

    private final ShorteningStrategy strategy;

    public URLShortener(ShorteningStrategy strategy) {
        this.strategy = strategy;
    }

    public String shorten(String longUrl) {
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        return longToShort.computeIfAbsent(longUrl, key -> {
            String shortUrl = strategy.generateShortUrl(key);
            while(shortToLong.containsKey(shortUrl)){
                shortUrl = strategy.generateShortUrl(ThreadLocalRandom.current().nextInt() + key);
            }
            shortToLong.put(shortUrl, key);
            return shortUrl;
        });
    }

    public String unShorten(String shortUrl) {
        return shortToLong.get(shortUrl);
    }

    protected Map<String, String> retrieveLongUrls(){
        return longToShort;
    }

    protected Map<String, String> retrieveShortUrls(){
        return shortToLong;
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
