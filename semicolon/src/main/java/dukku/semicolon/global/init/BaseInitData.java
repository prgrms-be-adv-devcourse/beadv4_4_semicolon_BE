package dukku.semicolon.global.init;

import dukku.common.shared.order.type.OrderItemStatus;
import dukku.common.shared.order.type.OrderStatus;
import dukku.common.shared.payment.type.PaymentStatus;
import dukku.common.shared.payment.type.PaymentType;
import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.semicolon.boundedContext.order.entity.Order;
import dukku.semicolon.boundedContext.order.entity.OrderItem;
import dukku.semicolon.boundedContext.order.out.OrderItemRepository;
import dukku.semicolon.boundedContext.order.out.OrderRepository;
import dukku.semicolon.boundedContext.payment.entity.Payment;
import dukku.semicolon.boundedContext.payment.out.PaymentRepository;
import dukku.semicolon.boundedContext.product.app.cqrs.SaveToElasticSearchUseCase;
import dukku.semicolon.boundedContext.product.entity.*;
import dukku.semicolon.boundedContext.product.out.*;
import dukku.semicolon.boundedContext.user.entity.Address;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import dukku.semicolon.boundedContext.user.out.AddressRepository;
import dukku.semicolon.boundedContext.user.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Configuration
@Profile({ "dev", "release" })
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class BaseInitData implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductSellerRepository productSellerRepository;
    private final ProductUserRepository productUserRepository;
    private final ProductLikeRepository productLikeRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SaveToElasticSearchUseCase saveToElasticSearchUseCase;

    private final List<User> users = new ArrayList<>();
    private final List<ProductUser> productUsers = new ArrayList<>();
    private final List<Category> childCategories = new ArrayList<>();
    private final List<Product> allProducts = new ArrayList<>();
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking initial data... Category count: {}", categoryRepository.count());
        // 시스템의 뼈대인 카테고리가 없거나 상품이 너무 적으면 초기화 진행 (안전장치 강화)
        if (categoryRepository.count() > 0 && productRepository.count() >= 400) {
            log.info("Data already exists (Category: {}, Product: {}). Skipping initialization.",
                    categoryRepository.count(), productRepository.count());
            return;
        }

        try {
            log.info("Starting data initialization...");
            createUsersAndAddresses();
            createCategories();
            createProductsAndInteractions();
            createOrdersAndPayments();
            log.info("Data initialization completed successfully! ⚖️");
        } catch (Exception e) {
            log.error("Failed to initialize data! Rolling back...", e);
            throw e;
        }
    }

    private void createUsersAndAddresses() {
        log.info("Creating users and addresses...");
        String encodedPassword = passwordEncoder.encode("1234");
        String[] intros = {
                "레어 아이템 수집가입니다. ⚖️", "아끼던 물건들 정리해요.", "믿고 거래하세요.",
                "캠핑 매니아입니다.", "기타 에디터입니다.", "굿즈 소중히 보관함."
        };

        for (int i = 1; i <= 20; i++) {
            User user = userRepository.saveAndFlush(User.builder()
                    .email("user" + i + "@test.com")
                    .password(encodedPassword)
                    .nickname("취미가" + i)
                    .role(Role.USER)
                    .build());
            users.add(user);

            // ProductUser & ProductSeller
            ProductUser pu = productUserRepository.saveAndFlush(ProductUser.create(user.getUuid(), user.getNickname()));
            productUsers.add(pu);
            productSellerRepository
                    .saveAndFlush(ProductSeller.create(user.getUuid(), intros[random.nextInt(intros.length)]));

            // Address
            addressRepository.saveAndFlush(Address.builder()
                    .userUuid(user.getUuid())
                    .receiverName(user.getNickname())
                    .receiverPhone("010-" + (random.nextInt(9000) + 1000) + "-" + (random.nextInt(9000) + 1000))
                    .zipcode("06" + random.nextInt(900))
                    .address1("서울특별시 강남구 테헤란로 " + (random.nextInt(500) + 1))
                    .address2(random.nextInt(100) + "동 " + random.nextInt(2000) + "호")
                    .isDefault(true)
                    .build());
        }
    }

    private void createCategories() {
        log.info("Creating categories...");
        String[] roots = { "애니메이션/굿즈", "게임/콘솔", "K-POP/아이돌", "트레이딩 카드 (TCG)", "아웃도어/스포츠", "악기/음악장비", "한정판 패션" };
        for (String rootName : roots) {
            Category root = categoryRepository.saveAndFlush(Category.createRoot(rootName));
            String[] children = getChildNames(rootName);
            for (String childName : children) {
                childCategories.add(categoryRepository.saveAndFlush(Category.createChild(childName, root)));
            }
        }
    }

    private String[] getChildNames(String root) {
        if (root.contains("애니메이션"))
            return new String[] { "피규어/프라모델", "인형/봉제인형", "포스터/문구" };
        if (root.contains("게임"))
            return new String[] { "닌텐도/포켓몬", "플레이스테이션/PC", "레트로 게임" };
        if (root.contains("K-POP"))
            return new String[] { "포토카드", "앨범/CD", "응원봉/굿즈" };
        if (root.contains("트레이딩"))
            return new String[] { "포켓몬 카드", "유희왕 카드", "매직 더 개더링" };
        if (root.contains("아웃도어"))
            return new String[] { "캠핑/차박", "등산/백패킹", "낚시/레저" };
        if (root.contains("악기"))
            return new String[] { "기타/베이스", "키보드/피아노", "오디오/DJ장비" };
        if (root.contains("패션"))
            return new String[] { "스니커즈", "한정판 의류", "한정판 액세서리" };
        return new String[] { "기타" };
    }

    private void createProductsAndInteractions() {
        log.info("Creating products and interactions... Total categories: {}", childCategories.size());
        for (Category child : childCategories) {
            String categoryName = child.getCategoryName();
            String[] titles = getTitlesByCategory(categoryName);
            String[] descriptions = getDescriptionsByCategory(categoryName);
            String[] images = getImagesByCategory(categoryName);

            // 카테고리당 20개로 증대 (총 420개 이상)
            for (int i = 0; i < 20; i++) {
                User seller = users.get(random.nextInt(users.size()));
                Product product = Product.create(
                        seller.getUuid(),
                        child,
                        titles[random.nextInt(titles.length)] + (i > 5 ? " (Special Edition)" : " (Mint)"),
                        descriptions[random.nextInt(descriptions.length)],
                        getRealisticPrice(categoryName),
                        3000L,
                        ConditionStatus.values()[random.nextInt(ConditionStatus.values().length)]);
                // 소스 이미지 URL에 이미 최적화 파라미터가 포함되어 있으므로 그대로 사용
                product.addImage(images[random.nextInt(images.length)]);
                Product savedProduct = productRepository.saveAndFlush(product);
                allProducts.add(savedProduct);

                // ES 동기화 추가 및 로그 강화
                try {
                    saveToElasticSearchUseCase.execute(savedProduct);
                } catch (Exception e) {
                    log.error("Failed to index product to ES: {}", savedProduct.getId(), e);
                }
            }
        }

        log.info("Creating likes and carts for {} users... Total products: {}", users.size(), allProducts.size());
        // Like & Cart Interactions (중복 방지 로직 추가)
        for (User user : users) {
            ProductUser pu = productUsers.stream()
                    .filter(u -> u.getUserUuid().equals(user.getUuid()))
                    .findFirst().orElse(null);

            java.util.Set<Integer> likedProductIds = new java.util.HashSet<>();
            java.util.Set<Integer> cartProductIds = new java.util.HashSet<>();

            for (int i = 0; i < 30; i++) {
                Product p = allProducts.get(random.nextInt(allProducts.size()));

                // 찜하기 (최대 15개)
                if (likedProductIds.size() < 15 && !likedProductIds.contains(p.getId())) {
                    productLikeRepository.saveAndFlush(ProductLike.create(user.getUuid(), p));
                    likedProductIds.add(p.getId());
                }

                // 장바구니 (최대 7개)
                if (pu != null && cartProductIds.size() < 7 && !cartProductIds.contains(p.getId())) {
                    try {
                        cartRepository.saveAndFlush(Cart.createCart(pu, p));
                        cartProductIds.add(p.getId());
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    private void createOrdersAndPayments() {
        log.info("Creating orders and payments...");
        // 주문 건수도 60건으로 증대
        for (int i = 0; i < 60; i++) {
            Product product = allProducts.get(random.nextInt(allProducts.size()));
            if (product.getSaleStatus() != SaleStatus.ON_SALE)
                continue;

            User buyer = users.get(random.nextInt(users.size()));
            if (buyer.getUuid().equals(product.getSellerUuid()))
                continue;

            Address addr = addressRepository.findAll().stream()
                    .filter(a -> a.getUserUuid().equals(buyer.getUuid()))
                    .findFirst().orElse(null);

            Order order = orderRepository.saveAndFlush(Order.builder()
                    .userUuid(buyer.getUuid())
                    .totalAmount(product.getPrice().intValue())
                    .address(addr != null ? addr.getAddress1() + " " + addr.getAddress2()
                            : "서울시 테헤란로 " + random.nextInt(100))
                    .recipient(buyer.getNickname())
                    .contactNumber("010-1234-" + (random.nextInt(9000) + 1000))
                    .status(OrderStatus.PAID)
                    .build());

            orderItemRepository.saveAndFlush(OrderItem.builder()
                    .order(order)
                    .productUuid(product.getUuid())
                    .sellerUuid(product.getSellerUuid())
                    .productName(product.getTitle())
                    .productPrice(product.getPrice().intValue())
                    .imageUrl(product.getImages().get(0).getImageUrl())
                    .status(i % 3 == 0 ? OrderItemStatus.CONFIRMED : OrderItemStatus.PAYMENT_COMPLETED)
                    .build());

            paymentRepository.saveAndFlush(Payment.builder()
                    .orderUuid(order.getUuid())
                    .userUuid(buyer.getUuid())
                    .amount(product.getPrice())
                    .paymentDepositOrigin(0L)
                    .paymentDeposit(0L)
                    .amountPgOrigin(product.getPrice())
                    .amountPg(product.getPrice())
                    .paymentCouponTotal(0L)
                    .refundTotal(0L)
                    .paymentType(PaymentType.MIXED)
                    .paymentStatus(PaymentStatus.DONE)
                    .approvedAt(java.time.LocalDateTime.now())
                    .tossOrderId("toss_" + UUID.randomUUID().toString().substring(0, 10))
                    .build());

            if (i % 3 == 0)
                product.confirmSale(order.getUuid());
            else
                product.reserve(order.getUuid());
            productRepository.saveAndFlush(product);
        }
    }

    private String[] getTitlesByCategory(String c) {
        if (c.contains("피규어"))
            return new String[] {
                    "메탈 빌드 스트라이크 프리덤 SOUL BLUE", "PG 언리쉬드 퍼스트 건담", "넨도로이드 1000번 기념판",
                    "핫토이 마크 85 엔드게임", "알터 아스카 퀘스트", "굿스마일 세이버 릴리", "초합금魂 마징가 Z"
            };
        if (c.contains("카드") || c.contains("TCG"))
            return new String[] {
                    "포켓몬 리자몽 SAR PSA 10", "뮤 ex UR 미개봉", "유희왕 푸른 눈의 백룡 레전더리",
                    "매직더개더링 블랙 로터스 (재판)", "피카츄 프로모 25주년", "블랙 매지션 걸 20th", "피카츄 일러스트레이터 정품"
            };
        if (c.contains("포토카드") || c.contains("아이돌"))
            return new String[] {
                    "아이브 안유진 앨포 일괄", "카리나 럭드 미공포", "뉴진스 민지 어텐션 특전",
                    "BTS 정국 골든 앨범 포카", "르세라핌 김채원 피어리스", "아이즈원 한정 굿즈 박스", "에스파 윈터 새비지 럭키드로우"
            };
        if (c.contains("캠핑") || c.contains("아웃도어"))
            return new String[] {
                    "헬리녹스 체어원 블랙", "스노우피크 기가파워 스토브", "크레모아 멀티 페이스 L",
                    "네이처하이크 6.3 에어텐트", "베어본즈 에디슨 랜턴", "골제로 마이크로 플래시", "스탠리 워터저그 7.5L"
            };
        if (c.contains("기타") || c.contains("악기"))
            return new String[] {
                    "펜더 아메리칸 프로 II 스트랫", "깁슨 레스폴 스탠다드 50s", "마틴 D-28 오센틱",
                    "아이바네즈 RG 시리즈", "테일러 214ce-K", "보스 GT-1000 멀티이펙터", "마샬 DSL40C 진공관 앰프"
            };
        if (c.contains("스니커즈") || c.contains("패션"))
            return new String[] {
                    "조던 1 하이 시카고 2022", "이지부스트 350 V2 지브라", "뉴발란스 992 그레이",
                    "살로몬 XT-6 화이트", "나이키 x 사카이 베이퍼와플", "슈프림 박스로고 23FW", "아크테릭스 베타 LT"
            };
        return new String[] { "[급매] 희귀 한정판 소장용", "미개봉 새제품 풀박스", "구매 후 전시만 함", "구하기 힘든 극미중고", "개인 소장용 S급" };
    }

    private String[] getDescriptionsByCategory(String c) {
        return new String[] {
                "관상용으로만 보관해서 상태는 신품급입니다. 박스 풀구성이고 흠집 하나 없어요. ⚖️",
                "덕질 청산하느라 눈물을 머금고 판매합니다. 정말 소중하게 다루던 물건이에요.",
                "직구로 어렵게 구한 제품입니다. 국내에 몇 없는 귀한 매물이에요. 쿨거 시 택포!",
                "하자에 예민한 편이라 정말 깨끗하게 관리했습니다. 상세 사진 원하시면 챗 주세요.",
                "급하게 돈이 필요해서 처분합니다. 정말 평생 소장하려 했던 명품입니다.",
                "실물 깡패입니다. 무조건 만족하실 거예요. 정품 아닐 시 200% 보상합니다.",
                "사용감 거의 없는 AAA급 상태입니다. 좋은 주인 만났으면 좋겠네요."
        };
    }

    private String[] getImagesByCategory(String c) {
        String query = "?auto=format&fit=crop&q=80&w=800";
        if (c.contains("피규어"))
            return new String[] {
                    "https://images.unsplash.com/photo-1550745165-9bc0b252726f" + query, // 큐브릭 스타일
                    "https://images.unsplash.com/photo-1589487391730-58f20eb2c308" + query, // 피규어 정면
                    "https://images.unsplash.com/photo-1593085512500-5d55148d6f0d" + query, // 애니메이션 스타일
                    "https://images.unsplash.com/photo-1560941001-d4b52ad00ecc" + query, // 넨도로이드 (정상 확인)
                    "https://images.unsplash.com/photo-1612036782180-6f0b6cd846fe" + query // 배트맨/히어로
            };
        if (c.contains("카드"))
            return new String[] {
                    "https://images.unsplash.com/photo-1613771404721-1f92d799e49f" + query,
                    "https://images.unsplash.com/photo-1659247209212-0761e8884967" + query,
                    "https://images.unsplash.com/photo-1644788320092-15f7956bc84b" + query,
                    "https://images.unsplash.com/photo-1613771404784-3a5686aa2be3" + query,
                    "https://images.unsplash.com/photo-1511512578047-dfb367046420" + query // 추가 ID
            };
        if (c.contains("캠핑"))
            return new String[] {
                    "https://images.unsplash.com/photo-1523906834658-6e24ef2386f9" + query,
                    "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4" + query,
                    "https://images.unsplash.com/photo-1537225228614-56cc3556d7ed" + query,
                    "https://images.unsplash.com/photo-1526491109672-747449369b4d" + query,
                    "https://images.unsplash.com/photo-1478131143081-80f7f84ca84d" + query // 추가 ID
            };
        if (c.contains("악기"))
            return new String[] {
                    "https://images.unsplash.com/photo-1550291652-6ea9114a47b1" + query,
                    "https://images.unsplash.com/photo-1510915361894-db8b60106cb1" + query,
                    "https://images.unsplash.com/photo-1444464666168-49d633b86797" + query,
                    "https://images.unsplash.com/photo-1525201548942-d8b8bb097fb0" + query,
                    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4" + query // 추가 ID
            };
        if (c.contains("스니커즈") || c.contains("패션"))
            return new String[] {
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff" + query,
                    "https://images.unsplash.com/photo-1549439602-43ebca2327af" + query,
                    "https://images.unsplash.com/photo-1512374382149-4332c6c021de" + query,
                    "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519" + query,
                    "https://images.unsplash.com/photo-1560769629-975ec94e6a86" + query // 추가 ID
            };
        if (c.contains("게임"))
            return new String[] {
                    "https://images.unsplash.com/photo-1588619491740-496531392652" + query,
                    "https://images.unsplash.com/photo-1647444840438-d6020fd184ec" + query,
                    "https://images.unsplash.com/photo-1605906302474-3c85029a82dd" + query,
                    "https://images.unsplash.com/photo-1493711662062-fa541adb3fc8" + query,
                    "https://images.unsplash.com/photo-1550745165-9bc0b252726f" + query // 추가 ID
            };
        return new String[] {
                "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2" + query,
                "https://images.unsplash.com/photo-1520004434532-6684162097cf" + query,
                "https://images.unsplash.com/photo-1510127034890-ba27508e9f1c" + query,
                "https://images.unsplash.com/photo-1484154218962-a197022b5858" + query
        };
    }

    private long getRealisticPrice(String c) {
        if (c.contains("피규어"))
            return (random.nextInt(50) + 10) * 10000L;
        if (c.contains("스니커즈"))
            return (random.nextInt(100) + 20) * 10000L;
        if (c.contains("카드"))
            return (random.nextInt(10) + 1) * 30000L;
        if (c.contains("악기"))
            return (random.nextInt(200) + 50) * 10000L;
        return (random.nextInt(100) + 1) * 10000L;
    }
}
