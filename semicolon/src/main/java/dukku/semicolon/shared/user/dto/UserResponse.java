package dukku.semicolon.shared.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.semicolon.boundedContext.user.entity.type.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class UserResponse {
    private UUID userUuid; // uuid
    private String email; // 이메일
    private String nickname; // 이름
    private Role role; // 권한
    private UserStatus status; // 계정 상태
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt; // 생성 날짜
}
