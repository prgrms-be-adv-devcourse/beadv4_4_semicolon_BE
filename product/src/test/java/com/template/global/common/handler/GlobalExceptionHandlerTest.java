package com.template.global.common.handler;

import com.template.global.exception.BadRequestException;
import com.template.global.exception.NotFoundException;
import com.template.global.handler.GlobalExceptionHandler;
import com.template.global.exception.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 테스트")
    void testHandleMethodArgumentNotValidException() {
        // given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getMessage()).thenReturn("Validation failed");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("입력 데이터에 오류가 있습니다.");
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getDetails()).isNotNull();
    }

    @Test
    @DisplayName("BindException 처리 테스트")
    void testHandleBindException() {
        // given
        BindException ex = new BindException(new Object(), "objectName");
        ex.addError(new FieldError("objectName", "field", "error message"));

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getStatus()).isEqualTo(422);
    }

    @Test
    @DisplayName("BaseException 처리 테스트")
    void testHandleBaseException() {
        // given
        NotFoundException ex = new NotFoundException("리소스를 찾을 수 없습니다.");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
        assertThat(response.getBody().getMessage()).isEqualTo("리소스를 찾을 수 없습니다.");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getDetails()).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("BadRequestException 처리 테스트")
    void testHandleBadRequestException() {
        // given
        BadRequestException ex = new BadRequestException("잘못된 요청입니다.");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBaseException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Exception 처리 테스트")
    void testHandleUnknownException() {
        // given
        Exception ex = new RuntimeException("알 수 없는 오류");

        // when
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnknownException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(response.getBody().getMessage()).isEqualTo("서버 오류가 발생했습니다.");
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getDetails()).isNull();
    }
}

