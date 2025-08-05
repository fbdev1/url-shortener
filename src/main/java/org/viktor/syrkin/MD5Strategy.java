package org.viktor.syrkin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Strategy implements ShorteningStrategy{

    @Override
    public String generateShortUrl(String longUrl) {
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(longUrl.getBytes());

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 4; i++){
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
