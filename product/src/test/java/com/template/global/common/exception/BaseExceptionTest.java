package com.template.global.common.exception;

import com.template.global.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseException 테스트")
class BaseExceptionTest {

    @Test
    @DisplayName("BaseException 상속 예외 생성 테스트")
    void testBaseExceptionSubclass() {
        // given
        String code = "TEST_ERROR";
        String message = "테스트 에러 메시지";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String details = "상세 정보";

        // when
        TestException exception = new TestException(code, message, status, details);

        // then
        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatus()).isEqualTo(status);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("BaseException details null 테스트")
    void testBaseExceptionWithNullDetails() {
        // given
        String code = "TEST_ERROR";
        String message = "테스트 에러 메시지";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // when
        TestException exception = new TestException(code, message, status, null);

        // then
        assertThat(exception.getCode()).isEqualTo(code);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatus()).isEqualTo(status);
        assertThat(exception.getDetails()).isNull();
    }

    @Test
    @DisplayName("BaseException 예외 메시지 전달 테스트")
    void testBaseExceptionMessage() {
        // given
        String message = "예외 메시지";

        // when
        TestException exception = new TestException("CODE", message, HttpStatus.BAD_REQUEST, null);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    static class TestException extends BaseException {
        public TestException(String code, String message, HttpStatus status, String details) {
            super(code, message, status, details);
        }
    }
}

