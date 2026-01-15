package dukku.common.global.auth.crypto.converter;

import dukku.common.global.auth.crypto.service.AesGcmCryptoService;
import dukku.common.global.auth.crypto.util.CryptoServiceHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class AesGcmConverterTest {

    private AesGcmConverter converter;
    private AesGcmCryptoService mockService;

    @BeforeEach
    void setUp() {
        // Mock AesGcmCryptoService 생성
        mockService = mock(AesGcmCryptoService.class);

        // CryptoServiceHolder를 Mock Service로 초기화
        // reflection으로 static instance 설정
        try {
            java.lang.reflect.Field instanceField = CryptoServiceHolder.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, mockService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        converter = new AesGcmConverter();
    }

    @Test
    @DisplayName("convertToDatabaseColumn - 평문 암호화 호출")
    void testConvertToDatabaseColumn() {
        String plainText = "hello";
        String encrypted = "encryptedText";

        when(mockService.encrypt(plainText)).thenReturn(encrypted);

        String result = converter.convertToDatabaseColumn(plainText);

        assertEquals(encrypted, result);
        verify(mockService, times(1)).encrypt(plainText);
    }

    @Test
    @DisplayName("convertToDatabaseColumn - null 입력 시 null 반환")
    void testConvertToDatabaseColumnNull() {
        String result = converter.convertToDatabaseColumn(null);
        assertNull(result);
        verifyNoInteractions(mockService);
    }

    @Test
    @DisplayName("convertToEntityAttribute - 암호문 복호화 호출")
    void testConvertToEntityAttribute() {
        String dbData = "encryptedText";
        String decrypted = "hello";

        when(mockService.decrypt(dbData)).thenReturn(decrypted);

        String result = converter.convertToEntityAttribute(dbData);

        assertEquals(decrypted, result);
        verify(mockService, times(1)).decrypt(dbData);
    }

    @Test
    @DisplayName("convertToEntityAttribute - null 입력 시 null 반환")
    void testConvertToEntityAttributeNull() {
        String result = converter.convertToEntityAttribute(null);
        assertNull(result);
        verifyNoInteractions(mockService);
    }
}
