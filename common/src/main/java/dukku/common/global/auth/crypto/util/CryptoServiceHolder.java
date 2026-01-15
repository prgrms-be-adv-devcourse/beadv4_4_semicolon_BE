package dukku.common.global.auth.crypto.util;

import dukku.common.global.auth.crypto.service.AesGcmCryptoService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * - JPA AttributeConverter는 JPA가 직접 인스턴스화하기 때문에 Spring DI로 AesGcmCryptoService를 주입받기 어렵다.
 * - 이 클래스는 Spring이 관리하는 AesGcmCryptoService를 static 필드로 보관하여 Converter에서 접근 가능하게 한다.
 *
 * 주의:
 *  - 애플리케이션 컨텍스트 초기화 순서에 따라 Converter가 이 Holder를 호출할 때
 *    아직 INSTANCE가 초기화되지 않았으면 IllegalStateException 발생.
 *  - 테스트 시에는 이 초기화 순서를 고려하여 컨텍스트를 띄우거나, Holder를 목(mock)으로 설정할 것.
 */
@Component
public class CryptoServiceHolder {

    private final AesGcmCryptoService service;
    private static AesGcmCryptoService instance;

    public CryptoServiceHolder(AesGcmCryptoService service) {
        this.service = service;
    }

    /**
     * Spring 컨텍스트 초기화 후 INSTANCE에 서비스 저장
     */
    @PostConstruct
    public void init() {
        instance = this.service;
    }

    /**
     * Converter가 호출할 수 있는 전역 접근자
     */
    public static AesGcmCryptoService get() {
        if (instance == null)
            throw new IllegalStateException("CryptoService not initialized");
        return instance;
    }
}
