package dukku.semicolon.global.auth.service;

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid password");
        }

        String token = authTokenIssuer.issue(
                user.getUuid(),   // ✅ UUID
                user.getRole()
        );

        return new TokenResponse(token);
    }
}
