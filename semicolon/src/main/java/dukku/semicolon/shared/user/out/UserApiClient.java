package dukku.semicolon.shared.user.out;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserApiClient {
    private final RestClient restClient;

    public UserApiClient(@Value("${custom.global.internalBackUrl}") String internalBackUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(internalBackUrl + "/api/v1/users")
                .build();
    }

    public String getRandomSecureTip() {
        return restClient.get()
                .uri("/randomSecureTip")
                .retrieve()
                .body(String.class);
    }
}
