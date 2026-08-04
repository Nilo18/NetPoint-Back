package com.netpoint.main.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;


@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    // name, email, role
    // jwt-ს ჰქმნის მომხმარებლისთვის, რაშიც შედის მისი აიდი და როლი
    public String generateToken(
            String userId, String companyId, String name, String email, String role,
                                String profileImage) {
        return Jwts.builder()
                .subject(userId)
                .claim("userId", userId)
                .claim("companyId", companyId)
                .claim("name", name)
                .claim("email", email)
                .claim("role", role)
                .claim("role", role)
                .claim("profileImage", profileImage)
//                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

//ტოკენიდან პოულობს მომხმარებლის როლს
    public <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver) {
        final io.jsonwebtoken.Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

//ტოკენიდან პოულობს მომხმარებლის აიდის
    public String extractUserId(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractCompanyId(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("companyId", String.class);
    }

//ამოწმებს ტოკენი ხო ვალიდურია და ვადა ხომ აქვს კიდევ
    public boolean isTokenValid(String token) {
        try {
            extractUserId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

