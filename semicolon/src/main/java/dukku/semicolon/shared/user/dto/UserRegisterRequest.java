package dukku.semicolon.shared.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+]{8,20}$",
            message = "비밀번호는 8~20자, 영문과 숫자를 포함해야 합니다."
    )
    private String password;

    private String nickname;
}
