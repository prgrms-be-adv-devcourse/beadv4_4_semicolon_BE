package dukku.semicolon.boundedContext.order.app.usecase;

import dukku.common.global.UserUtil;
import dukku.common.global.exception.BadRequestException;
import dukku.common.global.exception.UnauthorizedException;
import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import dukku.semicolon.shared.order.dto.SellerOrderItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSellerOrderListUseCase {
    private final OrderItemRepository orderItemRepository;

    /**
     * @param targetSellerUuid 조회하고자 하는 판매자의 UUID
     * @param pageable         페이징 정보
     */
    public Page<SellerOrderItemResponse> execute(UUID targetSellerUuid, Pageable pageable) {
        // 1. UUID 유효성 검사
        if (targetSellerUuid == null) {
            throw new BadRequestException("조회할 판매자 ID가 없습니다.");
        }

        // 2. 권한 검증 (가장 중요한 부분)
        validatePermission(targetSellerUuid);

        // 3. 조회 및 반환
        return orderItemRepository.findAllBySellerUuidWithOrder(targetSellerUuid, pageable)
                .map(SellerOrderItemResponse::from);
    }

    private void validatePermission(UUID targetSellerUuid) {
        // 관리자는 프리패스
        if (UserUtil.isAdmin()) {
            return;
        }

        // 일반 사용자는 본인의 데이터만 조회 가능
        UUID currentUserId = UserUtil.getUserId();
        if (!currentUserId.equals(targetSellerUuid)) {
            throw new UnauthorizedException("본인의 판매 내역만 조회할 수 있습니다.");
        }
    }
}