package dukku.semicolon.global.config;

import dukku.common.global.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * CSRF는 서버가 브라우저의 세션/쿠키를 신뢰할 때 공격 위험이 생김.
     * JWT는 Authorization 헤더에 직접 담기 때문에 쿠키 자동 전송과 무관 → CSRF 공격 불가능.
     * REST API + JWT 조합은 주로 비동기 호출(fetch, axios) 사용.
     * 브라우저 폼 기반 요청이 아니므로 CSRF 보호 대상 아님.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                /*
                  RESTful API는 무상태(stateless) 원칙
                  JWT 기반 인증에서는 서버가 상태(session)를 보존하지 않음 → 클라이언트가 JWT를 매 요청마다 전송
                  그러므로 세션은 필요없음
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/users/register", //임시로 v1추가
                                "/api/v1/auth/login" //임시테스트용
                               // "/api/v1/users/me" //삭제 임시 테스트
                        )
                        .permitAll() // 인증 필요없음 -> filter 미실행
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")// ADMIN만 접근

                        .anyRequest().authenticated() // 그 외는 인증 필요
                )
                .exceptionHandling(e -> e // 인증 실패시 예외 처리
                        .authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
