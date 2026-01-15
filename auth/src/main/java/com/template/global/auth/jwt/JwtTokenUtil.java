package com.template.global.auth.jwt;

import com.template.global.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenUtil {

    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 30L;            // 30분
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7L;  // 7일
    private static final String CLAIM_ROLE = "ROLE";

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtTokenUtil(
            @Value("${jwt.access.secret.key}") String accessSecret,
            @Value("${jwt.refresh.secret.key}") String refreshSecret
    ) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "INVALID_TOKEN";
        return token.substring(0, 8) + "...";
    }

    private String createToken(UUID userUuid, String role, long validity, SecretKey key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        return Jwts.builder()
                .subject(userUuid.toString())
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createAccessToken(UUID userUuid, String role) {
        return createToken(userUuid, role, ACCESS_TOKEN_VALIDITY, accessKey);
    }

    public String createRefreshToken(UUID userUuid, String role) {
        return createToken(userUuid, role, REFRESH_TOKEN_VALIDITY, refreshKey);
    }

    public boolean validateToken(String token, boolean isAccess) {
        try {
            SecretKey key = isAccess ? accessKey : refreshKey;

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 검증 실패: 이유={}, token[{}]", e.getMessage(), maskToken(token));
            return false;
        }
    }

    public UUID getUserUuid(String token, boolean isAccess) {
        return UUID.fromString(getClaims(token, isAccess).getSubject());
    }

    public String getRole(String token, boolean isAccess) {
        return getClaims(token, isAccess).get(CLAIM_ROLE, String.class);
    }

    private Claims getClaims(String token, boolean isAccess) {
        SecretKey key = isAccess ? accessKey : refreshKey;

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String refresh(String refreshToken) {
        if (!validateToken(refreshToken, false)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 정보 추출
        UUID userUuid = getUserUuid(refreshToken, false);
        String role = getRole(refreshToken, false);

        // 새로운 Access Token 발급 (기존 Role 유지)
        return createAccessToken(userUuid, role);
    }
}