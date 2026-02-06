package dukku.semicolon.shared.user.out;

import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.semicolon.shared.user.dto.UserNicknameResponse;
import dukku.semicolon.shared.user.dto.UserUuidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class UserApiClient {
    private final RestClient restClient;
    private final RestClient internalRestClient;

    public UserApiClient(@Value("${custom.global.internalBackUrl}") String internalBackUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(internalBackUrl + "/api/v1/users")
                .build();
        this.internalRestClient = RestClient.builder()
                .baseUrl(internalBackUrl + "/api/v1/internal/users")
                .build();
    }

    public String getRandomSecureTip() {
        return restClient.get()
                .uri("/randomSecureTip")
                .retrieve()
                .body(String.class);
    }

    public UserUuidResponse getUserUuidByRole(Role role) {
        return internalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uuid")
                        .queryParam("role", role)
                        .build())
                .retrieve()
                .body(UserUuidResponse.class);
    }

    public UserUuidResponse getUserUuidByEmail(String email) {
        return internalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uuid")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .body(UserUuidResponse.class);
    }

    public String getNickname(UUID userUuid) {
        UserNicknameResponse response = internalRestClient.get()
                .uri("/{userUuid}/nickname", userUuid)
                .retrieve()
                .body(UserNicknameResponse.class);

        return response != null ? response.getNickname() : null;
    }
}
