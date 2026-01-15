package dukku.common.global.auth.crypto.service;

import dukku.common.global.auth.crypto.config.CryptoConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmCryptoServiceTest {

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
    @DisplayName("잘못된 키 길이 입력시 예외 발생")
    void testInvalidKeyLength() throws Exception {
        // 16바이트 키 (AES-256은 32바이트 필요)
        byte[] shortKeyBytes = new byte[16];
        String shortBase64Key = Base64.getEncoder().encodeToString(shortKeyBytes);

        CryptoConfig config = createConfigWithKey(shortBase64Key);

        Exception ex = assertThrows(IllegalArgumentException.class, config::aesGcmCryptoService);
        assertTrue(ex.getMessage().contains("AES-256"));
    }


    @Test
    @DisplayName("빈 생성 후 암호화/복호화 정상 동작 확인")
    void testEncryptionDecryption() throws Exception {
        CryptoConfig config = createConfigWithKey(TEST_BASE64_KEY);
        AesGcmCryptoService cryptoService = config.aesGcmCryptoService();

        String plainText = "HelloUnitTest";

        String encrypted = cryptoService.encrypt(plainText);
        assertNotNull(encrypted);

        String decrypted = cryptoService.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }
}