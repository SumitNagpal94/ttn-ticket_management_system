package com.tms.common.security;

import com.tms.common.config.TmsConfig;
import com.tms.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final TmsConfig tmsConfig;

    public JwtService(TmsConfig tmsConfig) {
        this.tmsConfig = tmsConfig;
    }

    public String generateToken(String username, UserRole role, Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tmsConfig.getJwt().getExpirationMs());
        return Jwts.builder()
                .subject(username)
                .claims(Map.of("role", role.name(), "userId", userId != null ? userId : -1L))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = tmsConfig.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
