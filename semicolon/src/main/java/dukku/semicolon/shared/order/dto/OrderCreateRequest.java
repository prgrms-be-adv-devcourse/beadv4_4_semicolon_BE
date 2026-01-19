package dukku.semicolon.shared.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {
    @NotBlank
    private String address;
    @Size(max = 50)
    @NotBlank
    private String recipient;
    @Size(max = 50)
    @NotBlank
    private String contactNumber;
    @NotEmpty
    @Valid
    private List<OrderItemCreateRequest> items;

    @Getter
    @NoArgsConstructor
    public static class OrderItemCreateRequest {
        @NotNull
        private UUID productUuid;
        @NotNull
        private UUID sellerUuid;
        @Size(max = 100)
        @NotBlank
        private String productName;
        @Positive
        private int productPrice;
        private String imageUrl;
    }
}

