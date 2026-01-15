package dukku.common.global.auth.crypto.service;

import dukku.common.global.auth.crypto.exception.DecryptionException;
import dukku.common.global.auth.crypto.exception.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * - AES-256-GCM으로 암복호화 수행
 * - IV(Nonce): 12바이트 랜덤(매 암호화마다 새로 생성)
 * - 인증 태그: 128비트
 *
 * 암호문 포맷:
 *   base64(iv) + ":" + base64(ciphertextWithTag)
 *
 * 보안 주의:
 *  - 키(32바이트)는 외부에서 안전하게 주입되어야 함.
 *  - IV 재사용 금지(각 encrypt 호출 시 새로운 IV 생성).
 *  - 복호화 실패는 인증 태그 검증 실패(AEADBadTagException 포함)로 이어짐.
 */
public class AesGcmCryptoService {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits recommended
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 32바이트 AES 키 (AES-256)
    private final byte[] keyBytes;

    /**
     * 생성자: 키 길이 검증(AES-256 => 32 bytes)
     */
    public AesGcmCryptoService(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-256 requires 32-byte key");
        }
        this.keyBytes = keyBytes;
    }

    /**
     * 암호화:
     *  - 새 IV 생성
     *  - AES/GCM/NoPadding 초기화 후 평문 암호화
     *  - 반환 포맷: base64(iv) : base64(ciphertextWithTag)
     */
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);

            SecretKey key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV와 암호문(태그 포함)을 각각 Base64로 인코딩해 ":"로 결합
            return Base64.getEncoder().encodeToString(iv)
                    + ":" + Base64.getEncoder().encodeToString(cipherText);
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    /**
     * 복호화:
     *  - 저장된 포맷(base64(iv):base64(ct)) 파싱
     *  - GCMParameterSpec으로 복호화 초기화 후 평문 반환
     *  - 인증 실패 시 예외 발생
     */
    public String decrypt(String stored) {
        if (stored == null) return null;
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("invalid ciphertext format");

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            SecretKey key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // AEAD 태그 검증 실패(변조 의심) 또는 포맷 불일치 등
            throw new DecryptionException(e);
        }
    }
}
