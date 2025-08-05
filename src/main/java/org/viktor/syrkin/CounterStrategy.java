package org.viktor.syrkin;

import java.util.concurrent.atomic.AtomicLong;

public class CounterStrategy implements ShorteningStrategy {
    private final AtomicLong counter = new AtomicLong(1);
    @Override
    public String generateShortUrl(String longUrl) {
        return String.valueOf(counter.getAndIncrement());
    }
}
