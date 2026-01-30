package dukku.semicolon.shared.product.dto.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CartCreateRequest(
        @NotNull(message = "productUuid는 필수입니다")
        UUID productUuid,

        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다")
        String productName,

        @NotNull(message = "상품 가격은 필수입니다")
        @Positive(message = "상품 가격은 0보다 커야 합니다")
        Integer productPrice,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다")
        String imageUrl
) {}