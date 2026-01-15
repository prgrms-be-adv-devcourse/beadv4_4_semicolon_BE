package com.template.global.auth.crypto.config;

import com.template.global.auth.crypto.exception.DecryptionException;
import com.template.global.auth.crypto.service.AesGcmCryptoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class CryptoConfigTest {

    private static final String TEST_BASE64_KEY = "lGCfIAPebEerr2SH6W4Jy/63+K46CvAnWJkvFOPLIbo=";

    private CryptoConfig createConfigWithKey(String base64Key) throws Exception {
        CryptoConfig config = new CryptoConfig();

        // private field base64Key에 reflection으로 값 설정
        Field keyField = CryptoConfig.class.getDeclaredField("base64Key");
        keyField.setAccessible(true);
        keyField.set(config, base64Key);

        return config;
    }

    @Test
    @DisplayName("AesGcmCryptoService 빈 생성 확인")
    void testBeanCreation() throws Exception {
        CryptoConfig config = createConfigWithKey(TEST_BASE64_KEY);

        AesGcmCryptoService cryptoService = config.aesGcmCryptoService();
        assertNotNull(cryptoService);
    }

    @Test
    @DisplayName("잘못된 Base64 키 입력시 예외 발생")
    void testInvalidBase64Key() throws Exception {
        CryptoConfig config = createConfigWithKey("invalid_base64_string");

        Exception ex = assertThrows(IllegalArgumentException.class, config::aesGcmCryptoService);
        assertTrue(ex.getMessage().contains("Illegal base64"));
    }

    @Test
    @DisplayName("null 입력 암호화 시 null 반환")
    void testEncryptNullInput() throws Exception {
        CryptoConfig config = createConfigWithKey(TEST_BASE64_KEY);
        AesGcmCryptoService cryptoService = config.aesGcmCryptoService();

        String result = cryptoService.encrypt(null);
        assertNull(result);
    }

    @Test
    @DisplayName("복호화 시 잘못된 포맷 입력 예외 발생")
    void testDecryptInvalidFormat() throws Exception {
        CryptoConfig config = createConfigWithKey(TEST_BASE64_KEY);
        AesGcmCryptoService cryptoService = config.aesGcmCryptoService();

        // ":" 구분자가 없는 잘못된 암호문 포맷
        String invalidCipher = "not_a_valid_ciphertext";

        Exception ex = assertThrows(DecryptionException.class, () -> cryptoService.decrypt(invalidCipher));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("복호화 실패"));
    }

    @Test
    @DisplayName("복호화 시 변조된 암호문 입력 예외 발생 (AEAD 태그 검증 실패)")
    void testDecryptTamperedCiphertext() throws Exception {
        CryptoConfig config = createConfigWithKey(TEST_BASE64_KEY);
        AesGcmCryptoService cryptoService = config.aesGcmCryptoService();

        String plainText = "SensitiveData";
        String encrypted = cryptoService.encrypt(plainText);

        // 암호문 일부 변조 (Base64 문자열 중 한 글자 변경)
        String[] parts = encrypted.split(":");
        String tamperedCipher = parts[0] + ":" + parts[1].substring(0, parts[1].length() - 2) + "AA";

        assertThrows(DecryptionException.class, () -> cryptoService.decrypt(tamperedCipher));
    }
}
