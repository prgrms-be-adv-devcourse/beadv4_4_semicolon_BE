package dukku.common.global.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "custom.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private List<Policy> policies = new ArrayList<>();

    @Getter
    @Setter
    public static class Policy {
        private String name;
        private String pathPattern;
        private List<String> methods = new ArrayList<>();
        private long limit;
        private long windowSeconds;
        private RateLimitScope scope = RateLimitScope.USER_OR_IP;
        private boolean enabled = true;
    }
}
