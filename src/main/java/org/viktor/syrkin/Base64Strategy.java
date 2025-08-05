package org.viktor.syrkin;

import java.util.Base64;

public class Base64Strategy implements ShorteningStrategy{
    @Override
    public String generateShortUrl(String longUrl) {
        String urlWithoutProtocol = longUrl.replaceFirst("http", "");
        return Base64.getUrlEncoder()
                     .encodeToString(urlWithoutProtocol.getBytes())
                     .substring(0, 8);
    }
}
