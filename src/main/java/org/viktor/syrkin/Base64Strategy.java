package org.viktor.syrkin;

import java.util.Base64;

public class Base64Strategy implements ShorteningStrategy{
    @Override
    public String generateShortUrl(String longUrl) {
        return Base64.getUrlEncoder()
                     .encodeToString(longUrl.getBytes())
                     .substring(0, 8);
    }
}
