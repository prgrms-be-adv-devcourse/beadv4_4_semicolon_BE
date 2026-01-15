package dukku.common.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 커스텀 예외의 최상위 추상 클래스.
 * 공통으로 사용할 에러 코드, HTTP 상태 코드, 필드별 상세 오류 정보를 포함한다.
 * 모든 비즈니스 예외는 이 클래스를 상속받아 사용하도록 설계한다.
 */
@Getter
public abstract class BaseException extends RuntimeException {
    private final String code;                      // 에러 코드 (예: "VALIDATION_ERROR")
    private final HttpStatus status;                // HTTP 응답 상태 코드
    private final String details;  // 필드별 상세 에러 정보 목록 (없을 수 있음)

    /**
     * 상세 정보를 포함한 예외 생성자.
     *
     * @param code    에러 코드
     * @param message 예외 메시지
     * @param status  HTTP 상태 코드
     * @param details 필드별 상세 오류 리스트 (null 가능)
     */
    protected BaseException(String code, String message, HttpStatus status, String details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }
}
