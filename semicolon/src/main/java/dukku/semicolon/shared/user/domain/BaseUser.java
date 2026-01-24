package dukku.semicolon.shared.user.domain;

import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.semicolon.boundedContext.user.entity.type.UserStatus;
import dukku.common.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public abstract class BaseUser extends BaseEntity<Integer> {


    @Column(length = 100, unique = true, nullable = false, comment = "이메일 (로그인 ID)")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "회원 권한 (회원, 관리자 등)")
    @Setter(AccessLevel.PROTECTED)
    private Role role;

    @Column(length = 100, comment = "이름")
    @Setter(AccessLevel.PROTECTED)
    private String nickname;

    @Column(nullable = false, comment = "계정 상태")
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PROTECTED)
    private UserStatus status;
}