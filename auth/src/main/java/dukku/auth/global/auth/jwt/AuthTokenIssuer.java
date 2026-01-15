package dukku.auth.global.auth.jwt;

import dukku.common.global.exception.UnauthorizedException;
import dukku.common.global.auth.jwt.JwtTokenUtil;
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
public class AuthTokenIssuer {

    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 30L;            // 30분
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7L;  // 7일
    private static final String CLAIM_ROLE = "ROLE";

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final JwtTokenUtil jwtValidator;

    public AuthTokenIssuer(
            @Value("${jwt.access.secret.key}") String accessSecret,
            @Value("${jwt.refresh.secret.key}") String refreshSecret,
            JwtTokenUtil jwtValidator // 생성자 주입
    ) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.jwtValidator = jwtValidator;
    }

    // === 1. 토큰 생성 로직 (Auth 전용) ===
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

    // === 2. Refresh Token 검증 로직 (Auth 전용) ===
    // Access Token 검증은 Common의 JwtTokenUtil에게 맡김
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(refreshKey) // Refresh Key로 검증
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // === 3. 토큰 재발급 로직 ===
    public String refresh(String refreshToken) {
        // 1. Refresh Token 유효성 검사
        if (!validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. 정보 추출 (Refresh Key 사용)
        Claims claims = Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        UUID userUuid = UUID.fromString(claims.getSubject());
        String role = claims.get(CLAIM_ROLE, String.class);

        // 3. 새 Access Token 발급
        return createAccessToken(userUuid, role);
    }
}