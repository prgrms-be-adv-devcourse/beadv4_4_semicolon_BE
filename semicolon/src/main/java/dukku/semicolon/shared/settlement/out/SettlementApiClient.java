package dukku.semicolon.shared.settlement.out;

import dukku.semicolon.shared.settlement.dto.SettlementDetailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;


@Component
public class SettlementApiClient {

    private final RestClient restClient;

    public SettlementApiClient(@Value("${custom.global.internalBackUrl}") String internalBackUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(internalBackUrl + "/api/v1/admin/settlements")
                .build();
    }

    public SettlementDetailResponse getSettlement(UUID settlementUuid) {
        return restClient.get()
                .uri("/{settlementUuid}", settlementUuid)
                .retrieve()
                .body(SettlementDetailResponse.class);
    }

    public SettlementDetailResponse[] getSettlementsBySeller(UUID sellerUuid) {
        return restClient.get()
                .uri("?sellerUuid={sellerUuid}", sellerUuid)
                .retrieve()
                .body(SettlementDetailResponse[].class);
    }
}
