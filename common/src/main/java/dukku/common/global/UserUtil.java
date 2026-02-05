package dukku.common.global;

import dukku.common.global.auth.detail.CustomUserDetails;
import dukku.common.global.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class UserUtil {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private UserUtil() {
    }

    private static CustomUserDetails getCustomUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    public static UUID getUserId() {
        return getCustomUserDetails().getUserUuid();
    }

    public static String getRole() {
        CustomUserDetails userDetails = getCustomUserDetails();

        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("User has no role assigned."));
    }

    public static boolean isAdmin() {
        return ROLE_ADMIN.equals(getRole());
    }

    public static void validatePermission(UUID targetUuid) {
        // 관리자는 프리패스
        if (UserUtil.isAdmin()) {
            return;
        }

        // 일반 사용자는 본인의 데이터만 조회 가능
        UUID currentUserId = UserUtil.getUserId();
        if (!currentUserId.equals(targetUuid)) {
            throw new UnauthorizedException("본인만 조회할 수 있습니다.");
        }
    }
}