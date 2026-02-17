package dukku.common.global.ratelimit;

import dukku.common.global.auth.detail.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnProperty(prefix = "custom.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String TOO_MANY_REQUESTS_BODY =
            "{\"code\":\"TOO_MANY_REQUESTS\",\"message\":\"Too many requests. Please retry later.\"}";

    private final RateLimitProperties rateLimitProperties;
    private final RedisFixedWindowRateLimiter redisFixedWindowRateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        List<ResolvedPolicy> matchedPolicies = findMatchedPolicies(method, path);
        if (matchedPolicies.isEmpty()) {
            return true;
        }

        for (ResolvedPolicy policy : matchedPolicies) {
            String identifier = resolveIdentifier(policy.getScope(), request);

            RateLimitDecision decision;
            try {
                decision = redisFixedWindowRateLimiter.tryConsume(
                        policy.getName(),
                        identifier,
                        policy.getLimit(),
                        policy.getWindowSeconds()
                );
            } catch (Exception ex) {
                // Fail-open to avoid production outage when Redis is degraded.
                log.warn("Rate limit check skipped due to Redis error. policy={}, path={}", policy.getName(), path, ex);
                return true;
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(decision.retryAfterSeconds()));

            if (!decision.allowed()) {
                response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
                writeTooManyRequests(response);
                return false;
            }
        }

        return true;
    }

    private void writeTooManyRequests(HttpServletResponse response) {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(TOO_MANY_REQUESTS_BODY);
        } catch (Exception ex) {
            log.warn("Failed to write rate-limit response body", ex);
        }
    }

    private List<ResolvedPolicy> findMatchedPolicies(String method, String path) {
        List<RateLimitProperties.Policy> sourcePolicies = rateLimitProperties.getPolicies().isEmpty()
                ? defaultPolicies()
                : rateLimitProperties.getPolicies();

        List<ResolvedPolicy> matched = new ArrayList<>();
        for (RateLimitProperties.Policy policy : sourcePolicies) {
            if (!isEnabled(policy)) {
                continue;
            }
            if (!methodsMatch(policy.getMethods(), method)) {
                continue;
            }
            if (!PATH_MATCHER.match(policy.getPathPattern(), path)) {
                continue;
            }
            matched.add(new ResolvedPolicy(
                    normalizeName(policy.getName(), policy.getPathPattern(), policy.getMethods()),
                    policy.getScope(),
                    policy.getLimit(),
                    policy.getWindowSeconds()
            ));
        }

        return matched;
    }

    private boolean isEnabled(RateLimitProperties.Policy policy) {
        return policy.isEnabled()
                && StringUtils.hasText(policy.getPathPattern())
                && policy.getLimit() > 0L
                && policy.getWindowSeconds() > 0L;
    }

    private boolean methodsMatch(List<String> policyMethods, String requestMethod) {
        if (policyMethods == null || policyMethods.isEmpty()) {
            return true;
        }

        String normalizedMethod = requestMethod.toUpperCase(Locale.ROOT);
        for (String method : policyMethods) {
            if (normalizedMethod.equals(method.toUpperCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String resolveIdentifier(RateLimitScope scope, HttpServletRequest request) {
        return switch (scope) {
            case IP -> "ip:" + resolveClientIp(request);
            case USER -> "user:" + resolveUserId().orElse(resolveClientIp(request));
            case USER_OR_IP -> resolveUserId()
                    .map(userId -> "user:" + userId)
                    .orElse("ip:" + resolveClientIp(request));
        };
    }

    private Optional<String> resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUserUuid()).map(Object::toString);
        }

        return Optional.empty();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            int commaIndex = xForwardedFor.indexOf(',');
            return commaIndex > 0 ? xForwardedFor.substring(0, commaIndex).trim() : xForwardedFor.trim();
        }
        return request.getRemoteAddr();
    }

    private String normalizeName(String name, String pathPattern, List<String> methods) {
        if (StringUtils.hasText(name)) {
            return name.replaceAll("\\s+", "_").toLowerCase(Locale.ROOT);
        }

        String methodPart = (methods == null || methods.isEmpty())
                ? "all"
                : String.join("_", methods).toLowerCase(Locale.ROOT);
        String pathPart = pathPattern
                .replace("/", "_")
                .replace("*", "wild")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase(Locale.ROOT);
        return methodPart + pathPart;
    }

    private List<RateLimitProperties.Policy> defaultPolicies() {
        return List.of(
                policy("auth_login", "/api/v1/auth/login", List.of("POST"), 5, 60, RateLimitScope.IP),
                policy("auth_refresh", "/api/v1/auth/refresh", List.of("POST"), 10, 60, RateLimitScope.IP),
                policy("email_send_sustained", "/api/v1/users/email/send", List.of("POST"), 3, 600, RateLimitScope.IP),
                policy("email_send_burst", "/api/v1/users/email/send", List.of("POST"), 5, 60, RateLimitScope.IP),
                policy("order_create", "/api/v1/orders", List.of("POST"), 3, 10, RateLimitScope.USER_OR_IP),
                policy("payment_request", "/api/v1/payments/request", List.of("POST"), 3, 10, RateLimitScope.USER_OR_IP),
                policy("payment_confirm", "/api/v1/payments/confirm", List.of("POST"), 3, 10, RateLimitScope.USER_OR_IP),
                policy("payment_refund", "/api/v1/payments/refund", List.of("POST"), 3, 10, RateLimitScope.USER_OR_IP),
                policy("coupon_issue", "/api/v1/coupons/*/issue", List.of("POST"), 5, 60, RateLimitScope.USER_OR_IP),
                policy("coupon_use", "/api/v1/coupons/*/use", List.of("POST"), 10, 60, RateLimitScope.USER_OR_IP),
                policy("comment_create", "/api/v1/products/*/comments", List.of("POST"), 20, 60, RateLimitScope.USER_OR_IP),
                policy("comment_reply", "/api/v1/products/*/comments/*/replies", List.of("POST"), 20, 60, RateLimitScope.USER_OR_IP),
                policy("comment_update", "/api/v1/products/*/comments/*", List.of("PATCH"), 30, 60, RateLimitScope.USER_OR_IP),
                policy("comment_delete", "/api/v1/products/*/comments/*", List.of("DELETE"), 30, 60, RateLimitScope.USER_OR_IP),
                policy("product_like", "/api/v1/products/*/like", List.of("POST"), 60, 60, RateLimitScope.USER_OR_IP),
                policy("product_unlike", "/api/v1/products/*/like", List.of("DELETE"), 60, 60, RateLimitScope.USER_OR_IP),
                policy("seller_follow", "/api/v1/sellers/*/follow", List.of("POST"), 30, 60, RateLimitScope.USER_OR_IP),
                policy("seller_unfollow", "/api/v1/sellers/*/follow", List.of("DELETE"), 30, 60, RateLimitScope.USER_OR_IP),
                policy("seller_review_create", "/api/v1/seller-reviews", List.of("POST"), 10, 60, RateLimitScope.USER_OR_IP),
                policy("seller_review_update", "/api/v1/seller-reviews/*", List.of("PATCH"), 20, 60, RateLimitScope.USER_OR_IP),
                policy("seller_review_delete", "/api/v1/seller-reviews/*", List.of("DELETE"), 20, 60, RateLimitScope.USER_OR_IP)
        );
    }

    private RateLimitProperties.Policy policy(
            String name,
            String pathPattern,
            List<String> methods,
            long limit,
            long windowSeconds,
            RateLimitScope scope
    ) {
        RateLimitProperties.Policy policy = new RateLimitProperties.Policy();
        policy.setName(name);
        policy.setPathPattern(pathPattern);
        policy.setMethods(methods);
        policy.setLimit(limit);
        policy.setWindowSeconds(windowSeconds);
        policy.setScope(scope);
        policy.setEnabled(true);
        return policy;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ResolvedPolicy {
        private final String name;
        private final RateLimitScope scope;
        private final long limit;
        private final long windowSeconds;
    }
}
