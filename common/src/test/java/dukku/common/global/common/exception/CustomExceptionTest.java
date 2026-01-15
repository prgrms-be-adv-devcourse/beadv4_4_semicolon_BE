package dukku.common.global.common.exception;

import dukku.common.global.exception.*;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("커스텀 Exception 테스트")
class CustomExceptionTest {

    @Test
    @DisplayName("BadRequestException 생성 테스트")
    void testBadRequestException() {
        // given
        String details = "잘못된 요청 상세 정보";

        // when
        BadRequestException exception = new BadRequestException(details);

        // then
        assertThat(exception.getCode()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
        assertThat(exception.getMessage()).isEqualTo("잘못된 요청");
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("ConflictException 생성 테스트")
    void testConflictException() {
        // given
        String details = "충돌 상세 정보";

        // when
        ConflictException exception = new ConflictException(details);

        // then
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("ForbiddenException 생성 테스트")
    void testForbiddenException() {
        // given
        String details = "권한 없음 상세 정보";

        // when
        ForbiddenException exception = new ForbiddenException(details);

        // then
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("NotFoundException 생성 테스트")
    void testNotFoundException() {
        // given
        String details = "리소스를 찾을 수 없음";

        // when
        NotFoundException exception = new NotFoundException(details);

        // then
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("UnauthorizedException 생성 테스트")
    void testUnauthorizedException() {
        // given
        String details = "인증 필요";

        // when
        UnauthorizedException exception = new UnauthorizedException(details);

        // then
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("ValidationException 생성 테스트")
    void testValidationException() {
        // given
        String details = "유효성 검증 실패";

        // when
        ValidationException exception = new ValidationException(details);

        // then
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(exception.getDetails()).isEqualTo(details);
    }
}

