package com.steve.secure_auth;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class KeyCheck {
    public static void main(String[] args) {
        String base64Secret = "v2DQ/qdsoALW2JjL8a86ENcoBIE1QWAm2sIn10LJa1U=";
        byte[] decodedKey = Base64.getDecoder().decode(base64Secret);

        SecretKey key = Keys.hmacShaKeyFor(decodedKey); // validates size for HS256/HS512

        System.out.println("Key algorithm: " + key.getAlgorithm());
        System.out.println("Key length: " + decodedKey.length * 8 + " bits");
    }
}
