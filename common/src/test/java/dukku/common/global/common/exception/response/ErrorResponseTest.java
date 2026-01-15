package dukku.common.global.common.exception.response;

import dukku.common.global.exception.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse 테스트")
class ErrorResponseTest {

    @Test
    @DisplayName("ErrorResponse 생성 (details 포함) 테스트")
    void testCreateErrorResponseWithDetails() {
        // given
        String code = "VALIDATION_ERROR";
        String message = "입력 데이터에 오류가 있습니다.";
        int status = 422;
        String details = "필드별 오류 정보";

        // when
        ErrorResponse response = new ErrorResponse(code, message, status, details);

        // then
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("ErrorResponse 생성 (details 미포함) 테스트")
    void testCreateErrorResponseWithoutDetails() {
        // given
        String code = "NOT_FOUND";
        String message = "리소스를 찾을 수 없습니다.";
        int status = 404;

        // when
        ErrorResponse response = new ErrorResponse(code, message, status);

        // then
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getDetails()).isNull();
    }

    @Test
    @DisplayName("ErrorResponse 여러 상태 코드 테스트")
    void testCreateErrorResponseWithDifferentStatusCodes() {
        // given & when & then
        ErrorResponse badRequest = new ErrorResponse("BAD_REQUEST", "잘못된 요청", 400);
        assertThat(badRequest.getStatus()).isEqualTo(400);

        ErrorResponse unauthorized = new ErrorResponse("UNAUTHORIZED", "인증 필요", 401);
        assertThat(unauthorized.getStatus()).isEqualTo(401);

        ErrorResponse forbidden = new ErrorResponse("FORBIDDEN", "권한 없음", 403);
        assertThat(forbidden.getStatus()).isEqualTo(403);

        ErrorResponse notFound = new ErrorResponse("NOT_FOUND", "리소스 없음", 404);
        assertThat(notFound.getStatus()).isEqualTo(404);

        ErrorResponse conflict = new ErrorResponse("CONFLICT", "충돌", 409);
        assertThat(conflict.getStatus()).isEqualTo(409);

        ErrorResponse internalError = new ErrorResponse("INTERNAL_ERROR", "서버 오류", 500);
        assertThat(internalError.getStatus()).isEqualTo(500);
    }
}

