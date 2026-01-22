package dukku.semicolon.shared.product.dto;

import dukku.common.shared.product.type.ConditionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

        @NotNull
        @Min(1)
        private Integer categoryId;

        @NotBlank
        @Size(max = 200)
        private String title;

        private String description;

        @NotNull
        @PositiveOrZero
        private Long price;

        @PositiveOrZero
        private Long shippingFee;

        @Size(max = 10)
        private List<@NotBlank String> imageUrls;

        @NotNull
        private ConditionStatus conditionStatus;
}
