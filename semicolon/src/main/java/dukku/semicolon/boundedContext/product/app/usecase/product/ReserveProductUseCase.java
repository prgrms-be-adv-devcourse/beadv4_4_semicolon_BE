package dukku.semicolon.boundedContext.product.app.usecase.product;

import dukku.semicolon.boundedContext.product.entity.Product;
import dukku.semicolon.boundedContext.product.out.ProductRepository;
import dukku.semicolon.shared.product.dto.product.ProductReserveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public void execute(ProductReserveRequest request) {
        // 1. 상품 리스트 조회
        List<Product> products = productRepository.findAllByUuidIn(request.productUuids());

        // 검증: 요청한 상품 수와 조회된 상품 수가 같은지 (유효하지 않은 UUID 체크)
        if (products.size() != request.productUuids().size()) {
            log.error("ReserveProducUseCase Error. Product Size: {}", products.size());

            throw new IllegalArgumentException("일부 상품을 찾을 수 없습니다.");
        }

        // 2. 상태 변경 (Dirty Checking으로 자동 저장)
        // TODO: 이미 판매 또는 예약된 경우라면?
        for (Product product : products) {
            product.reserve(request.orderUuid());
        }
    }
}
