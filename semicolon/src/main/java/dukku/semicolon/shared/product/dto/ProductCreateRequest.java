package dukku.semicolon.shared.product.dto;

import dukku.common.shared.product.type.ConditionStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductCreateRequest(

        @NotNull
        @Min(1)
        Integer categoryId,

        @NotBlank
        @Size(max = 200)
        String title,

        String description,

        @NotNull
        @PositiveOrZero
        Long price,

        @PositiveOrZero
        Long shippingFee,

        @Size(max = 10)
        List<@NotBlank String> imageUrls,

        @NotNull
        ConditionStatus conditionStatus
) {}
