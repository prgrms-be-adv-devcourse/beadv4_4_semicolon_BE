package dukku.common.global.auth.crypto.converter;

import dukku.common.global.auth.crypto.util.CryptoServiceHolder;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter: 엔티티 필드(String 평문) <-> DB 컬럼(String 암호문) 자동 변환
 *
 * 사용법:
 *  @Convert(converter = AesGcmConverter.class) 를 엔티티 필드에 지정하면 JPA가 자동으로 호출한다.
 *
 * 주의:
 *  - AttributeConverter는 JPA가 직접 인스턴스화하므로 Spring 빈 주입이 어렵다.
 *    CryptoServiceHolder를 통해 Spring-managed AesGcmCryptoService 인스턴스에 접근한다.
 *  - 대량 조회 시 복호화 비용이 크므로 필요한 컬럼에만 적용할 것.
 */
@Converter
public class AesGcmConverter implements AttributeConverter<String, String> {

    /**
     * 엔티티 -> DB: 평문을 암호화하여 DB에 저장할 문자열로 반환
     * 반환 포맷은 AesGcmCryptoService 내부 정의(예: base64(iv):base64(ct))
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return CryptoServiceHolder.get().encrypt(attribute); // runtime 시점 호출
    }

    /**
     * DB -> 엔티티: DB에 저장된 암호문을 복호화하여 평문을 반환
     * 복호화 실패 시 RuntimeException으로 감싸서 전파된다.
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return CryptoServiceHolder.get().decrypt(dbData); // runtime 시점 호출
    }
}
