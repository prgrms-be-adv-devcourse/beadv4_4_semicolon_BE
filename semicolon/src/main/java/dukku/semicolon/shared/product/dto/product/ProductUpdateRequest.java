package dukku.semicolon.shared.product.dto.product;

import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductUpdateRequest(
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Integer categoryId,

        @NotBlank(message = "제목은 비어있을 수 없습니다.")
        @Size(max = 200, message = "제목은 200자를 넘을 수 없습니다.")
        String title,

        String description,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Long price,

        @Min(value = 0, message = "배송비는 0원 이상이어야 합니다.")
        Long shippingFee,

        @NotNull(message = "상품 상태는 필수입니다.")
        ConditionStatus conditionStatus,

        @NotNull(message = "노출 상태는 필수입니다.")
        VisibilityStatus visibilityStatus,

        // 리스트 자체는 null일 수 있어도, 내부의 URL 문자열은 빈 값이면 안 된다는 검증 예시
        List<@NotBlank(message = "이미지 URL은 비어있을 수 없습니다.") String> imageUrls
) {}