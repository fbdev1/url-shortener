package org.viktor.syrkin;

import java.util.concurrent.ThreadLocalRandom;

public class RandomStrategy implements ShorteningStrategy{
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 8;

    @Override
    public String generateShortUrl(String longUrl) {
        StringBuilder sb = new StringBuilder(LENGTH);
        for(int i = 0; i < LENGTH; i++){
            sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
