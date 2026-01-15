package dukku.common.global.auth.crypto;

import dukku.common.global.auth.crypto.converter.AesGcmConverter;
import dukku.common.global.auth.crypto.service.AesGcmCryptoService;
import dukku.common.global.auth.crypto.util.CryptoServiceHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class CryptoTest {

    private static AesGcmCryptoService cryptoService;
    private static AesGcmConverter converter;

    @BeforeAll
    static void setup() {
        // AES-256 테스트용 키 생성 (32바이트)
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < keyBytes.length; i++) keyBytes[i] = (byte) i;

        cryptoService = new AesGcmCryptoService(keyBytes);

        CryptoServiceHolder holder = new CryptoServiceHolder(cryptoService);
        holder.init();

        converter = new AesGcmConverter();
    }

    @Test
    void testEncryptionAndDecryption() {
        String plainText = "Hello, AES-GCM!";
        String encrypted = cryptoService.encrypt(plainText);
        assertNotNull(encrypted, "Encrypted text should not be null");

        String decrypted = cryptoService.decrypt(encrypted);
        assertEquals(plainText, decrypted, "Decrypted text should match original");
    }

    @Test
    void testConverterRoundTrip() {
        String original = "SensitiveData123";

        // 엔티티 -> DB
        String dbValue = converter.convertToDatabaseColumn(original);
        assertNotNull(dbValue);

        // DB -> 엔티티
        String entityValue = converter.convertToEntityAttribute(dbValue);
        assertEquals(original, entityValue);
    }

    @Test
    void testNullHandling() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(cryptoService.encrypt(null));
        assertNull(cryptoService.decrypt(null));
    }

    @Test
    void testInvalidCiphertext() {
        String invalidCipher = "invalid:format";
        Exception ex = assertThrows(RuntimeException.class, () -> cryptoService.decrypt(invalidCipher));
        assertTrue(ex.getMessage().contains("복호화 실패"));
    }
}
