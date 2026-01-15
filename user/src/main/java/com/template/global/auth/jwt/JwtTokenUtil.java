package com.template.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenUtil {

    private static final String CLAIM_ROLE = "ROLE";
    private final SecretKey accessKey;

    public JwtTokenUtil(@Value("${jwt.access.secret.key}") String accessSecret) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "INVALID_TOKEN";
        return token.substring(0, 8) + "...";
    }

    // 검증 로직 (Access Token 전용)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 검증 실패: 이유={}, token[{}]", e.getMessage(), maskToken(token));
            return false;
        }
    }

    public UUID getUserUuid(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    public String getRole(String token) {
        return getClaims(token).get(CLAIM_ROLE, String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}