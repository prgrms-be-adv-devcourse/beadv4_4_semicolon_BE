package dukku.product.boundedContext.product.app.usecase.product;

import dukku.common.shared.product.dto.product.ProductDetailResponse;
import dukku.common.shared.product.exception.ProductNotFoundException;
import dukku.common.shared.user.dto.UserProfileResponse;
import dukku.common.shared.user.out.UserApiClient;
import dukku.product.boundedContext.product.app.cqrs.ProductStatsRedisSupport;
import dukku.product.boundedContext.product.app.support.ProductMapper;
import dukku.product.boundedContext.product.entity.Product;
import dukku.product.boundedContext.product.entity.ProductSeller;
import dukku.product.boundedContext.product.entity.ProductUser;
import dukku.product.boundedContext.product.out.ProductRepository;
import dukku.product.boundedContext.product.out.ProductSellerRepository;
import dukku.product.boundedContext.product.out.ProductUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindProductDetailUseCase {
    private final ProductRepository productRepository;
    private final ProductStatsRedisSupport productStatsRedisSupport;
    private final ProductSellerRepository productSellerRepository;
    private final ProductUserRepository productUserRepository;
    private final UserApiClient userApiClient;

    @Transactional
    public ProductDetailResponse execute(UUID productUuid) {
        log.info("[FindProductDetailUseCase] 상품 상세 조회 시작. productUuid={}", productUuid);

        Product product = productRepository.findByUuidWithImagesAndCategory(productUuid)
                .filter(p -> p.getDeletedAt() == null)
                .or(() -> {
                    log.warn("[FindProductDetailUseCase] 패치 조인 쿼리로 상품 조회 실패. 기본 조회(findByUuid)를 시도합니다. productUuid={}",
                            productUuid);
                    return productRepository.findByUuidAndDeletedAtIsNull(productUuid);
                })
                .orElseThrow(() -> {
                    log.error("[FindProductDetailUseCase] 어떤 방식으로도 상품을 찾을 수 없습니다. productUuid={}", productUuid);
                    return new ProductNotFoundException();
                });

        log.info("[FindProductDetailUseCase] 상품 조회 성공. title={}, sellerUuid={}", product.getTitle(),
                product.getSellerUuid());

        productStatsRedisSupport.incrementView(product.getId());

        // 상점 정보 조회: 이벤트 지연/누락으로 ProductSeller가 없으면 즉시 생성
        ProductSeller seller = productSellerRepository.findByUserUuid(product.getSellerUuid())
                .orElseGet(() -> {
                    log.warn("[FindProductDetailUseCase] 상점 정보(ProductSeller)가 없어 자동 생성합니다. userUuid={}, productUuid={}",
                            product.getSellerUuid(), productUuid);
                    return productSellerRepository.save(ProductSeller.create(product.getSellerUuid(), "반가워요! 내 상점입니다."));
                });

        log.info("[FindProductDetailUseCase] 상점 정보 조회 성공. sellerUserUuid={}", seller.getUserUuid());

        String nickname = resolveNicknameWithBackfill(seller.getUserUuid());

        log.info("[FindProductDetailUseCase] 최종 조회 완료. nickname={}", nickname);

        return ProductMapper.toDetail(product, seller, nickname);
    }

    private String resolveNicknameWithBackfill(UUID userUuid) {
        return productUserRepository.findById(userUuid)
                .map(ProductUser::getNickname)
                .filter(StringUtils::hasText)
                .orElseGet(() -> fetchAndBackfillNickname(userUuid));
    }

    private String fetchAndBackfillNickname(UUID userUuid) {
        try {
            UserProfileResponse profile = userApiClient.getUserProfile(userUuid);
            String nickname = profile == null ? null : profile.getNickname();
            if (!StringUtils.hasText(nickname)) {
                return "이름없음";
            }

            if (!productUserRepository.existsById(userUuid)) {
                try {
                    productUserRepository.save(ProductUser.create(userUuid, nickname));
                } catch (Exception e) {
                    log.warn("[FindProductDetailUseCase] ProductUser 보정 저장 실패. userUuid={}", userUuid, e);
                }
            }

            return nickname;
        } catch (Exception e) {
            log.warn("[FindProductDetailUseCase] 유저 프로필 조회 실패로 기본 닉네임 사용. userUuid={}", userUuid, e);
            return "이름없음";
        }
    }
}
