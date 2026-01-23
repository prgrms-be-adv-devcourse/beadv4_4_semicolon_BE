package dukku.semicolon.shared.product.dto;

import dukku.common.shared.product.type.ConditionStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Min(1)
    private Integer categoryId;

    @Size(max = 200)
    private String title;

    private String description;

    @PositiveOrZero
    private Long price;

    @PositiveOrZero
    private Long shippingFee;

    @Size(max = 10)
    private List<@NotBlank String> imageUrls;

    private ConditionStatus conditionStatus;
}
