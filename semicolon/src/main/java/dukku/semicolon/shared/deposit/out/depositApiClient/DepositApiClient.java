package dukku.semicolon.shared.deposit.out.depositApiClient;

import dukku.semicolon.shared.deposit.dto.DepositAccountResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class DepositApiClient {

    private final RestClient restClient;

    public DepositApiClient(@Value("${custom.global.internalBackUrl}") String internalBackUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(internalBackUrl + "/api/v1/admin/deposits")
                .build();
    }

    public UUID getDepositUuid(UUID userUuid) {
        DepositAccountResponse response = restClient.get()
                .uri("/{userUuid}/account", userUuid)
                .retrieve()
                .body(DepositAccountResponse.class);

        if (response != null && response.getData() != null) {
            return response.getData().getDepositUuid();
        }

        // TODO: 적절한 예외 처리 (혹은 null 반환)
        return null;
    }
}
