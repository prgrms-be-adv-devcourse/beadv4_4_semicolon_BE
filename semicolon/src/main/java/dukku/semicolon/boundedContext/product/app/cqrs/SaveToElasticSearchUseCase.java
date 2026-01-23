package dukku.semicolon.boundedContext.product.app.cqrs;

import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.entity.ProductImage;
import dukku.semicolon.boundedContext.product.entity.query.ProductDocument;
import dukku.semicolon.boundedContext.product.out.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveToElasticSearchUseCase {
    private final ProductSearchRepository productSearchRepository;

    public void execute(Product product) {
        // A. 정렬 점수 계산 (품절이면 1, 아니면 0) -> CQRS Write 모델 최적화
        int sortPriority = (product.getSaleStatus() == SaleStatus.SOLD_OUT) ? 1 : 0;

        // C. 썸네일 추출
        String thumbnail = product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        // D. 문서 빌드
        ProductDocument document = ProductDocument.builder()
                .id(String.valueOf(product.getId()))
                .productUuid(product.getUuid().toString())
                .saleSortPriority(sortPriority)
                .title(product.getTitle())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .price(product.getPrice())
                .saleStatus(product.getSaleStatus())
                .visibilityStatus(product.getVisibilityStatus())
                .likeCount(product.getLikeCount())
                .createdAt(product.getCreatedAt())
                .thumbnailImageUrl(thumbnail)
                .build();

        productSearchRepository.save(document);
    }
}
