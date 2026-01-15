package dukku.common.global.auth.crypto.util;

import dukku.common.global.auth.crypto.service.AesGcmCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CryptoServiceHolderTest {

    private AesGcmCryptoService mockService;

    @BeforeEach
    void setUp() {
        // 테스트용 AesGcmCryptoService Mock (실제 구현 대신 간단 객체)
        mockService = new AesGcmCryptoService(new byte[32]); // AES-256용 32바이트 키
    }

    @Test
    @DisplayName("PostConstruct init 후 get() 호출 시 동일한 서비스 반환")
    void testInitAndGet() {
        CryptoServiceHolder holder = new CryptoServiceHolder(mockService);
        holder.init(); // @PostConstruct 대신 직접 호출

        AesGcmCryptoService returnedService = CryptoServiceHolder.get();
        assertNotNull(returnedService);
        assertSame(mockService, returnedService);
    }

    @Test
    @DisplayName("여러 번 init 호출 시 마지막 서비스가 적용됨")
    void testMultipleInitCalls() {
        CryptoServiceHolder holder1 = new CryptoServiceHolder(mockService);
        holder1.init();

        byte[] anotherKey = new byte[32];
        AesGcmCryptoService anotherService = new AesGcmCryptoService(anotherKey);
        CryptoServiceHolder holder2 = new CryptoServiceHolder(anotherService);
        holder2.init();

        // 마지막 init에서 주입된 서비스가 반환되어야 함
        assertSame(anotherService, CryptoServiceHolder.get());
    }
}
