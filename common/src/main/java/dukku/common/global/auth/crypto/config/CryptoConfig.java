package dukku.common.global.auth.crypto.config;

import dukku.common.global.auth.crypto.service.AesGcmCryptoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * 애플리케이션에서 사용할 AesGcmCryptoService 빈을 생성한다.
 */
@Configuration
public class CryptoConfig {

    /**
     * Base64 인코딩된 AES-256 키 (예: openssl rand -base64 32)
     */
    @Value("${crypto.key}")
    private String base64Key;

    /**
     * AesGcmCryptoService 빈 생성
     *  - base64Key를 디코드하여 바이트 배열로 전달
     *  - 생성자에서 키 길이(32바이트)를 검증함
     */
    @Bean
    public AesGcmCryptoService aesGcmCryptoService() {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new AesGcmCryptoService(keyBytes);
    }
}
