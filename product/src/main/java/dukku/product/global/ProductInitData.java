package dukku.product.global;

import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.product.boundedContext.product.entity.Category;
import dukku.product.boundedContext.product.entity.Product;
import dukku.product.boundedContext.product.entity.ProductSeller;
import dukku.product.boundedContext.product.entity.ProductUser;
import dukku.product.boundedContext.product.entity.query.ProductDocument;
import dukku.product.boundedContext.product.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "product.init.enabled", havingValue = "true", matchIfMissing = false)
public class ProductInitData {

    private final ProductRepository productRepository;
    private final ProductSellerRepository productSellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductUserRepository productUserRepository;
    private final ProductSearchRepository productSearchRepository;

    private final Map<String, UUID> userMap = new HashMap<>();
    private final Map<String, UUID> sellerMap = new HashMap<>();
    private final Map<String, Product> productMap = new HashMap<>();

    private static final String IMG_PHONE = "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_LAPTOP = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_TABLET = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_HEADPHONE = "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_SPEAKER = "https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_AMP = "https://images.unsplash.com/photo-1558470598-a5dda9640f68?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_TENT = "https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_CHAIR = "https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_LAMP = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_ALBUM = "https://images.unsplash.com/photo-1619983081563-430f63602796?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_CAMERA = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&q=80&w=500";

    @Bean
    @Order(2)
    public CommandLineRunner initProducts() {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                log.info("🚀 [InitData] Data Initialization Started");

                if (productRepository.count() > 0) {
                    log.info("[InitData] Existing products found. Skip initialization.");
                    return;
                }

                userMap.clear();
                sellerMap.clear();
                productMap.clear();

                initCategoryHierarchy();
                initUsersAndSellers();

                Map<String, String> catNameMap = getCategoryNameMap();
                createProducts(catNameMap);

                log.info("✅ [InitData] Initialization Completed.");
            }
        };
    }

    private void initCategoryHierarchy() {
        if (categoryRepository.count() > 0) return;

        Category electronics = categoryRepository.save(Category.createRoot("전자기기"));
        Category smartphone = categoryRepository.save(Category.createChild("스마트폰", electronics));
        categoryRepository.save(Category.createChild("아이폰", smartphone));
        categoryRepository.save(Category.createChild("삼성", smartphone));
        categoryRepository.save(Category.createChild("기타", smartphone));

        Category tablet = categoryRepository.save(Category.createChild("태블릿", electronics));
        categoryRepository.save(Category.createChild("아이패드", tablet));
        categoryRepository.save(Category.createChild("갤럭시탭", tablet));

        Category computer = categoryRepository.save(Category.createChild("PC/노트북", electronics));
        categoryRepository.save(Category.createChild("맥북", computer));
        categoryRepository.save(Category.createChild("일반 노트북", computer));
        categoryRepository.save(Category.createChild("PC부품", computer));

        Category etcElec = categoryRepository.save(Category.createChild("기타 가전", electronics));
        categoryRepository.save(Category.createChild("웨어러블", etcElec));
        categoryRepository.save(Category.createChild("액션캠", etcElec));

        Category camping = categoryRepository.save(Category.createRoot("캠핑/레저"));
        Category tentGroup = categoryRepository.save(Category.createChild("텐트/타프", camping));
        categoryRepository.save(Category.createChild("돔/거실형", tentGroup));
        categoryRepository.save(Category.createChild("기타 텐트", tentGroup));
        categoryRepository.save(Category.createChild("타프", tentGroup));

        Category campingFurniture = categoryRepository.save(Category.createChild("캠핑가구", camping));
        categoryRepository.save(Category.createChild("테이블", campingFurniture));
        categoryRepository.save(Category.createChild("의자", campingFurniture));

        Category campingGear = categoryRepository.save(Category.createChild("캠핑소품", camping));
        categoryRepository.save(Category.createChild("랜턴/조명", campingGear));
        categoryRepository.save(Category.createChild("취사용품", campingGear));
        categoryRepository.save(Category.createChild("침낭/매트", campingGear));

        Category instruments = categoryRepository.save(Category.createRoot("악기/음향"));
        Category strings = categoryRepository.save(Category.createChild("현악기", instruments));
        categoryRepository.save(Category.createChild("일렉기타", strings));
        categoryRepository.save(Category.createChild("통기타", strings));
        categoryRepository.save(Category.createChild("베이스", strings));

        Category audioGear = categoryRepository.save(Category.createChild("음향기기", instruments));
        categoryRepository.save(Category.createChild("헤드폰/이어폰", audioGear));
        categoryRepository.save(Category.createChild("스피커", audioGear));
        categoryRepository.save(Category.createChild("앰프/DAC", audioGear));

        Category keyboards = categoryRepository.save(Category.createChild("건반악기", instruments));
        categoryRepository.save(Category.createChild("피아노/신디", keyboards));

        Category camera = categoryRepository.save(Category.createRoot("카메라/렌즈"));
        Category digitalCam = categoryRepository.save(Category.createChild("디지털 카메라", camera));
        categoryRepository.save(Category.createChild("DSLR/미러리스", digitalCam));
        categoryRepository.save(Category.createChild("하이엔드/컴팩트", digitalCam));

        Category cameraParts = categoryRepository.save(Category.createChild("렌즈/주변기기", camera));
        categoryRepository.save(Category.createChild("교환렌즈", cameraParts));
        categoryRepository.save(Category.createChild("삼각대/액세서리", cameraParts));

        Category filmGroup = categoryRepository.save(Category.createChild("필름카메라", camera));
        categoryRepository.save(Category.createChild("필름 바디", filmGroup));

        Category golf = categoryRepository.save(Category.createRoot("골프"));
        Category golfClub = categoryRepository.save(Category.createChild("골프채", golf));
        categoryRepository.save(Category.createChild("드라이버", golfClub));
        categoryRepository.save(Category.createChild("우드/유틸", golfClub));
        categoryRepository.save(Category.createChild("아이언", golfClub));
        categoryRepository.save(Category.createChild("웨지", golfClub));
        categoryRepository.save(Category.createChild("퍼터", golfClub));

        Category golfAccGroup = categoryRepository.save(Category.createChild("용품/의류", golf));
        categoryRepository.save(Category.createChild("골프백", golfAccGroup));
        categoryRepository.save(Category.createChild("골프웨어", golfAccGroup));
        categoryRepository.save(Category.createChild("기타용품", golfAccGroup));

        Category goods = categoryRepository.save(Category.createRoot("스타굿즈"));
        Category idolBoy = categoryRepository.save(Category.createChild("보이그룹", goods));
        categoryRepository.save(Category.createChild("BTS", idolBoy));
        categoryRepository.save(Category.createChild("세븐틴", idolBoy));
        categoryRepository.save(Category.createChild("스트레이키즈", idolBoy));

        Category idolGirl = categoryRepository.save(Category.createChild("걸그룹", goods));
        categoryRepository.save(Category.createChild("뉴진스", idolGirl));
        categoryRepository.save(Category.createChild("IVE", idolGirl));
        categoryRepository.save(Category.createChild("aespa", idolGirl));

        Category goodsCommon = categoryRepository.save(Category.createChild("교통수단/일반", goods));
        categoryRepository.save(Category.createChild("앨범", goodsCommon));
        categoryRepository.save(Category.createChild("포토카드", goodsCommon));
        categoryRepository.save(Category.createChild("콘서트티켓", goodsCommon));

        log.info("📂 [InitData] Category Hierarchy Created.");
    }

    private void initUsersAndSellers() {
        createSeller("s1", "u1", "세미콜론", 4.5, "깔끔한 거래 원해요", 3, 2, 1);
        createSeller("s2", "u2", "테크마스터", 4.9, "전자기기 전문", 154, 12, 2);
        createSeller("s3", "u3", "소리사랑", 4.8, "음향기기 수집가", 89, 8, 3);
        createSeller("s4", "u4", "숲속의집", 4.7, "감성 캠핑 용품", 210, 25, 4);
        createSeller("s5", "u5", "나이스샷", 4.6, "골프 클럽 거래", 67, 15, 5);
        createSeller("s6", "u6", "최애보관소", 5.0, "K-POP 굿즈", 320, 40, 6);
        createSeller("s7", "u7", "찰칵찰칵", 4.2, null, 12, 2, 7);
        createSeller("s8", "u8", "라이더", 4.0, null, 8, 1, 8);
        createSeller("s9", "u9", "책벌레", 4.5, null, 45, 5, 9);
        createSeller("s10", "u10", "겜돌이", 4.8, null, 23, 4, 10);
        createSeller("s11", "u11", "강태공", 3.5, null, 3, 1, 11);
        createSeller("s12", "u12", "요리왕", 4.1, null, 15, 2, 12);
        createSeller("s13", "u13", "블럭쌓기", 4.9, null, 67, 6, 13);
        createSeller("s14", "u14", "슈즈홀릭", 4.3, null, 22, 3, 14);
        createSeller("s15", "u15", "가방조아", 4.7, null, 9, 2, 15);
        createSeller("s16", "u16", "식집사", 4.4, null, 18, 4, 16);
        createSeller("s17", "u17", "차마시는날", 5.0, null, 4, 1, 17);
        createSeller("s18", "u18", "득근득근", 4.0, null, 11, 2, 18);
        createSeller("s19", "u19", "그림쟁이", 4.6, null, 5, 1, 19);
        createSeller("s20", "u20", "레트로매니아", 4.8, null, 56, 7, 20);
    }

    private void createSeller(String sId, String uId, String nickname, double rating, String intro, int sales, int active, int seed) {
        String uuidStr = String.format("00000000-0000-0000-0000-%012d", seed);
        UUID userUuid = UUID.fromString(uuidStr);

        // User 생성 및 체크
        if (!productUserRepository.existsById(userUuid)) {
            productUserRepository.save(ProductUser.create(userUuid, nickname));
        }
        userMap.put(uId, userUuid);

        // 수정된 Seller 체크 로직: 반드시 userUuid로 조회해서 확인
        productSellerRepository.findByUserUuid(userUuid).ifPresentOrElse(
                existing -> {
                    log.info("ℹ️ Seller already exists for user: {}", userUuid);
                    sellerMap.put(sId, existing.getSellerUuid());
                },
                () -> {
                    ProductSeller seller = ProductSeller.builder()
                            .sellerUuid(UUID.randomUUID())
                            .userUuid(userUuid)
                            .intro(intro)
                            .salesCount(sales)
                            .activeListingCount(active)
                            .averageRating(BigDecimal.valueOf(rating))
                            .reviewCount(0)
                            .build();
                    productSellerRepository.save(seller);
                    sellerMap.put(sId, seller.getSellerUuid());
                }
        );
    }

    private void createProducts(Map<String, String> catNameMap) {
        saveProduct("p-s1-1", "chair", "s1", "헬리녹스 체어제로 블랙", "초경량 백패킹 체어입니다. 2회 사용했고 상태 좋습니다.", 180000L, 3000L, ConditionStatus.MINOR_WEAR, SaleStatus.SOLD_OUT, 156, 12, 4, 45, catNameMap, IMG_CHAIR);
        saveProduct("p-s1-2", "audio-headphone", "s1", "소니 WF-1000XM5 무선이어폰", "소니 플래그십 이어폰. 박스 풀구성.", 280000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.SOLD_OUT, 234, 18, 6, 30, catNameMap, IMG_HEADPHONE);
        saveProduct("p1", "phone-apple", "s2", "아이폰 15 프로 맥스 256GB 자급제", "미개봉 새제품입니다. 자급제.", 1550000L, 0L, ConditionStatus.SEALED, SaleStatus.ON_SALE, 320, 8, 2, 5, catNameMap, IMG_PHONE);
        saveProduct("p2", "phone-samsung", "s2", "갤럭시 S24 울트라 512GB 티타늄블랙", "개봉 후 1회 통화만 했습니다.", 1350000L, 3000L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 180, 6, 1, 4, catNameMap, IMG_PHONE);
        saveProduct("p3", "tablet-ipad", "s2", "아이패드 프로 12.9 M2 256GB", "애플케어 2025년까지.", 1100000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 95, 5, 2, 7, catNameMap, IMG_TABLET);
        saveProduct("p5", "laptop-macbook", "s2", "맥북 프로 14인치 M3 Pro 18GB", "박스 풀구성.", 2800000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 412, 10, 3, 14, catNameMap, IMG_LAPTOP);
        saveProduct("p10", "audio-headphone", "s3", "소니 WH-1000XM5 무선 헤드폰", "노캔 최강.", 320000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 245, 9, 3, 10, catNameMap, IMG_HEADPHONE);
        saveProduct("p19", "tent", "s4", "스노우피크 랜드록 텐트", "패밀리 캠핑 최고.", 1800000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 356, 10, 3, 15, catNameMap, IMG_TENT);
        saveProduct("p37", "album", "s6", "뉴진스 2nd EP Get Up 미개봉", "한정판 버니.", 35000L, 2000L, ConditionStatus.SEALED, SaleStatus.ON_SALE, 456, 10, 3, 10, catNameMap, IMG_ALBUM);
        saveProduct("p46", "digital-camera", "s7", "소니 A7IV 바디 셔터 1만컷", "풀프레임 미러리스.", 2200000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 178, 6, 2, 9, catNameMap, IMG_CAMERA);
    }

    private void saveProduct(String pId, String catCode, String sId, String title, String desc, Long price, Long shipFee, ConditionStatus condition, SaleStatus saleStatus, int view, int like, int comment, int daysAgo, Map<String, String> catNameMap, String imageUrl) {
        String categoryName = catNameMap.getOrDefault(catCode, "기타");
        Category category = categoryRepository.findByCategoryName(categoryName).orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
        UUID sellerUuid = sellerMap.get(sId);

        boolean exists = productRepository.existsBySellerUuidAndCategory_IdAndTitleAndPriceAndDeletedAtIsNull(
                sellerUuid, category.getId(), title, price)
                || productRepository.existsByCategory_IdAndTitleAndPriceAndDeletedAtIsNull(
                category.getId(), title, price);
        if (exists) {
            log.info("[InitData] 중복 상품 생성 스킵. sellerUuid={}, categoryId={}, title={}",
                    sellerUuid, category.getId(), title);
            return;
        }

        Product product = Product.builder()
                .sellerUuid(sellerUuid)
                .category(category)
                .title(title)
                .description(desc)
                .price(price)
                .shippingFee(shipFee)
                .conditionStatus(condition)
                .saleStatus(saleStatus)
                .visibilityStatus(VisibilityStatus.VISIBLE)
                .viewCount(view)
                .likeCount(like)
                .commentCount(comment)
                .createdAt(LocalDateTime.now().minusDays(daysAgo))
                .build();

        product.addImage(imageUrl);
        Product savedProduct = productRepository.save(product);
        productMap.put(pId, savedProduct);

        List<Integer> categoryPath = getCategoryPath(savedProduct.getCategory());
        ProductDocument document = ProductDocument.builder()
                .id(String.valueOf(savedProduct.getId()))
                .productUuid(savedProduct.getUuid().toString())
                .saleSortPriority(calculateSaleSortPriority(savedProduct.getSaleStatus()))
                .title(savedProduct.getTitle())
                .description(savedProduct.getDescription())
                .sellerUuid(savedProduct.getSellerUuid().toString())
                .categoryIds(categoryPath)
                .saleStatus(savedProduct.getSaleStatus())
                .visibilityStatus(savedProduct.getVisibilityStatus())
                .conditionStatus(savedProduct.getConditionStatus())
                .price(savedProduct.getPrice())
                .shippingFee(savedProduct.getShippingFee())
                .viewCount(savedProduct.getViewCount())
                .likeCount(savedProduct.getLikeCount())
                .commentCount(savedProduct.getCommentCount())
                .createdAt(savedProduct.getCreatedAt())
                .thumbnailImageUrl(imageUrl)
                .build();

        productSearchRepository.save(document);
    }

    private List<Integer> getCategoryPath(Category category) {
        List<Integer> path = new ArrayList<>();
        Category current = category;
        while (current != null) {
            path.add(current.getId().intValue());
            current = current.getParent();
        }
        return path;
    }

    private Integer calculateSaleSortPriority(SaleStatus status) {
        return (status == SaleStatus.ON_SALE || status == SaleStatus.RESERVED) ? 0 : 1;
    }

    private Map<String, String> getCategoryNameMap() {
        Map<String, String> map = new HashMap<>();
        map.put("electronics", "전자기기");
        map.put("smartphone", "스마트폰");
        map.put("phone-apple", "아이폰");
        map.put("phone-samsung", "삼성");
        map.put("tablet-ipad", "아이패드");
        map.put("laptop-macbook", "맥북");
        map.put("audio-headphone", "헤드폰/이어폰");
        map.put("tent", "텐트/타프");
        map.put("chair", "의자");
        map.put("album", "앨범");
        map.put("digital-camera", "디지털 카메라");
        return map;
    }
}
