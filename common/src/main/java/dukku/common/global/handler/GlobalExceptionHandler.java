package dukku.common.global.handler;

import dukku.common.global.exception.BaseException;
import dukku.common.global.exception.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 클래스.
 * 컨트롤러에서 발생하는 예외를 공통 형식의 JSON 응답으로 변환하여 반환한다.
 *
 * 에러 응답 JSON 로깅은 GlobalResponseWrapper에서 처리됩니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 유효성 검사 실패 시 발생하는 예외 처리.
     * MethodArgumentNotValidException 또는 BindException을 처리하며,
     * 필드별 오류 정보를 수집하여 표준 에러 응답을 생성한다.
     *
     * @param ex MethodArgumentNotValidException 또는 BindException 인스턴스
     * @return HTTP 400 상태와 표준화된 에러 응답
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationExceptions(Exception ex) {
        log.error("ValidationException: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "입력 데이터에 오류가 있습니다.", HttpStatus.BAD_REQUEST.value(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * BaseException 계열 예외 처리.
     * 커스텀 예외에서 제공하는 상태 코드, 에러 코드, 메시지, 상세 정보를 포함하여 응답 생성.
     *
     * @param ex BaseException 인스턴스
     * @return 표준화된 에러 응답과 HTTP 상태 코드
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.error("BaseException [{}]: {}", ex.getCode(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getStatus().value(), ex.getDetails());

        return ResponseEntity.status(ex.getStatus())
                .body(errorResponse);
    }

    /**
     * 알 수 없는 예외 처리.
     * 내부 로깅 후, 클라이언트에는 일반화된 서버 오류 메시지(INTERNAL_SERVER_ERROR)로 응답 반환.
     *
     * @param ex Exception 인스턴스
     * @return HTTP 500 상태와 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception ex) {
        log.error("UnhandledException: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

}
