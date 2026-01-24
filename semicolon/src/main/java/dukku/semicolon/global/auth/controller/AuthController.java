package dukku.semicolon.global.auth.controller;

import dukku.semicolon.global.auth.dto.LoginRequest;
import dukku.semicolon.global.auth.dto.TokenResponse;
import dukku.semicolon.global.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Validated LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
