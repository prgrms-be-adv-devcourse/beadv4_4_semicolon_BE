package dukku.semicolon.boundedContext.order.app;

import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import dukku.semicolon.shared.order.dto.SellerOrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSellerOrderListUseCase {
    private final OrderItemRepository orderItemRepository;

    public Page<SellerOrderItemResponse> execute(UUID sellerUuid, Pageable pageable) {
        return orderItemRepository.findAllBySellerUuidWithOrder(sellerUuid, pageable)
                .map(SellerOrderItemResponse::from);
    }
}
