package com.template.shared.user.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 회원(User) 관련 Swagger 전용 Meta-Annotation 모음
 */
public final class UserApiDocs {

    private UserApiDocs() {}

    // =============== 공통 태그 ===============
    @Documented
    @Target(TYPE)
    @Retention(RUNTIME)
    @Tag(
            name = "회원 관리 API",
            description = "회원 가입, 정보 조회/수정 및 비밀번호 변경 관련 기능"
    )
    public @interface UserTag {}

    // =============== 1) 회원가입 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임 등의 정보를 입력받아 새로운 일반 회원(USER)을 등록합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Register Request",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "password": "Password123!",
                                              "nickname": "홍길동"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "회원가입 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            value = """
                                                    {
                                                      "userUuid": "550e8400-e29b-41d4-a716-446655440000",
                                                      "email": "user@example.com",
                                                      "nickname": "홍길동",
                                                      "role": "USER",
                                                      "status": "ACTIVE"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (유효성 검사 실패 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"이메일 형식이 올바르지 않습니다.\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 존재하는 이메일",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"이미 가입된 이메일입니다.\"}")
                            )
                    )
            }
    )
    public @interface RegisterUser {}

    // =============== 2) 본인 정보 조회 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "본인 정보 조회",
            description = "현재 로그인한 사용자의 정보를 조회합니다. (Auth Token 필요)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "유저 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "User Info",
                                            value = """
                                                    {
                                                      "userUuid": "550e8400-e29b-41d4-a716-446655440000",
                                                      "email": "user@example.com",
                                                      "nickname": "홍길동",
                                                      "role": "USER",
                                                      "status": "ACTIVE"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 사용자",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"존재하지 않는 사용자입니다.\"}")
                            )
                    )
            }
    )
    public @interface GetMe {}

    // =============== 3) 본인 정보 수정 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "본인 정보 수정",
            description = "닉네임(name) 등의 정보를 수정합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Update Request",
                                    value = """
                                            {
                                              "name": "새로운닉네임"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "본인 정보 수정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Updated Info",
                                            value = """
                                                    {
                                                      "userUuid": "550e8400-e29b-41d4-a716-446655440000",
                                                      "email": "user@example.com",
                                                      "nickname": "새로운닉네임",
                                                      "role": "USER",
                                                      "status": "ACTIVE"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 사용자",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"존재하지 않는 사용자입니다.\"}")
                            )
                    )
            }
    )
    public @interface UpdateMe {}

    // =============== 4) 비밀번호 변경 ===============
    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @Operation(
            summary = "본인 비밀번호 수정",
            description = "본인 비밀번호를 수정합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Password Update",
                                    value = """
                                            {
                                              "currentPassword": "OldPassword123!",
                                              "newPassword": "NewPassword123!"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "본인 비밀번호 수정 성공 (No Content)"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "비밀번호 불일치",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"비밀번호 불일치.\"}")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 사용자",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"message\": \"존재하지 않는 사용자입니다.\"}")
                            )
                    )
            }
    )
    public @interface UpdatePassword {}
}