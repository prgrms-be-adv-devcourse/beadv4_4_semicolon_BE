package dukku.semicolon.shared.product.dto.cqrs;

import dukku.common.shared.product.type.ConditionStatus;
import lombok.*;

@Getter
@Setter // Controller 바인딩 및 Facade에서 값 설정을 위해 추가
@NoArgsConstructor // Spring 파라미터 바인딩을 위해 필수
@AllArgsConstructor
@Builder // Facade에서 편하게 객체를 만들기 위해 추가
public class ProductSearchRequest {

    private String keyword;
    private Integer categoryId;
    private Long minPrice;
    private Long maxPrice;
    private ConditionStatus conditionStatus;

    @Builder.Default // Builder 사용 시 기본값 적용
    private ProductSortType sortType = ProductSortType.LATEST;

    // 수동 생성자를 사용할 경우를 대비한 방어 로직 (선택 사항)
    // Spring이 Setter를 쓸 때는 이 로직 대신 필드 초기화(= ProductSortType.LATEST)가 작동합니다.
    public void setSortType(ProductSortType sortType) {
        this.sortType = (sortType != null) ? sortType : ProductSortType.LATEST;
    }
}