package dukku.semicolon.global.auth.service;

import dukku.common.global.exception.NotFoundException;
import dukku.common.global.exception.UnauthorizedException;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import dukku.semicolon.global.auth.dto.LoginRequest;
import dukku.semicolon.global.auth.dto.TokenResponse;
import dukku.semicolon.global.auth.jwt.AuthTokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;

    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->  new NotFoundException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new UnauthorizedException("비밀번호가 올바르지 않습니다.");
        }

        String token = authTokenIssuer.issue(
                user.getUuid(),
                user.getRole()
        );

        return new TokenResponse(token);
    }
}
