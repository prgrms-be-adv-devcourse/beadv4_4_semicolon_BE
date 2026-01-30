package dukku.semicolon.global;

import dukku.common.shared.product.type.ConditionStatus;
import dukku.common.shared.product.type.SaleStatus;
import dukku.common.shared.product.type.VisibilityStatus;
import dukku.semicolon.boundedContext.product.entity.*;
import dukku.semicolon.boundedContext.product.entity.query.ProductDocument;
import dukku.semicolon.boundedContext.product.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(2)
public class ProductInitData {

    private final ProductRepository productRepository;
    private final ProductSellerRepository productSellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductUserRepository productUserRepository;
    private final ProductSearchRepository productSearchRepository; // ES Repository

    // 관계 매핑을 위한 임시 저장소
    private final Map<String, UUID> userMap = new HashMap<>();
    private final Map<String, UUID> sellerMap = new HashMap<>();
    private final Map<String, Product> productMap = new HashMap<>();

    // 이미지 상수
    private static final String IMG_PHONE = "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_LAPTOP = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_TABLET = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_HEADPHONE = "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_SPEAKER = "https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_AMP = "https://images.unsplash.com/photo-1558470598-a5dda9640f68?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_TENT = "https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_CHAIR = "https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_LAMP = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_GOLF = "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_ALBUM = "https://images.unsplash.com/photo-1619983081563-430f63602796?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_PHOTOCARD = "https://images.unsplash.com/photo-1611162616305-c69b3fa7fbe0?auto=format&fit=crop&q=80&w=500";
    private static final String IMG_CAMERA = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&q=80&w=500";


    @Bean
    public CommandLineRunner initProducts() {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                // 이미 데이터가 있다면 초기화 스킵
                if (productRepository.count() > 0) {
                    return;
                }

                log.info("🚀 [InitData] Product & ES Data Initialization Started...");

                // 0. ES 인덱스 초기화 (개발 환경이므로 꼬임 방지용)
                productSearchRepository.deleteAll();

                // 1. 유저 및 판매자 생성
                initUsersAndSellers();

                // 2. 카테고리 매핑 테이블 준비
                Map<String, String> catNameMap = getCategoryNameMap();

                // 3. 상품 생성 및 ES 인덱싱
                createProducts(catNameMap);

                log.info("✅ [InitData] Initialization Completed (MySQL + Elasticsearch).");
            }
        };
    }

    private void initUsersAndSellers() {
        // 주요 판매자만 상세 생성 (나머지는 생략 가능)
        createSeller("s1", "u1", "세미콜론", 4.5, "깔끔한 거래 원해요", 3, 2);
        createSeller("s2", "u2", "테크마스터", 4.9, "전자기기 전문", 154, 12);
        createSeller("s3", "u3", "소리사랑", 4.8, "음향기기 수집가", 89, 8);
        createSeller("s4", "u4", "숲속의집", 4.7, "감성 캠핑 용품", 210, 25);
        createSeller("s5", "u5", "나이스샷", 4.6, "골프 클럽 거래", 67, 15);
        createSeller("s6", "u6", "최애보관소", 5.0, "K-POP 굿즈", 320, 40);
        createSeller("s7", "u7", "찰칵찰칵", 4.2, null, 12, 2);
        createSeller("s8", "u8", "라이더", 4.0, null, 8, 1);
        createSeller("s9", "u9", "책벌레", 4.5, null, 45, 5);
        createSeller("s10", "u10", "겜돌이", 4.8, null, 23, 4);
        createSeller("s11", "u11", "강태공", 3.5, null, 3, 1);
        createSeller("s12", "u12", "요리왕", 4.1, null, 15, 2);
        createSeller("s13", "u13", "블럭쌓기", 4.9, null, 67, 6);
        createSeller("s14", "u14", "슈즈홀릭", 4.3, null, 22, 3);
        createSeller("s15", "u15", "가방조아", 4.7, null, 9, 2);
        createSeller("s16", "u16", "식집사", 4.4, null, 18, 4);
        createSeller("s17", "u17", "차마시는날", 5.0, null, 4, 1);
        createSeller("s18", "u18", "득근득근", 4.0, null, 11, 2);
        createSeller("s19", "u19", "그림쟁이", 4.6, null, 5, 1);
        createSeller("s20", "u20", "레트로매니아", 4.8, null, 56, 7);
    }

    private void createSeller(String sId, String uId, String nickname, double rating, String intro, int sales, int active) {
        UUID uuid = UUID.randomUUID();
        // 유저 생성
        productUserRepository.save(ProductUser.create(uuid, nickname));
        userMap.put(uId, uuid);

        // 판매자 생성
        ProductSeller seller = ProductSeller.builder()
                .userUuid(uuid)
                .intro(intro)
                .salesCount(sales)
                .activeListingCount(active)
                .build();
        productSellerRepository.save(seller);
        sellerMap.put(sId, uuid);
    }

    private void createProducts(Map<String, String> catNameMap) {
        // ===== s1 세미콜론 =====
        saveProduct("p-s1-1", "chair", "s1", "헬리녹스 체어제로 블랙", "초경량 백패킹 체어입니다. 2회 사용했고 상태 좋습니다.", 180000L, 3000L, ConditionStatus.MINOR_WEAR, SaleStatus.SOLD_OUT, 156, 12, 4, 45, catNameMap, IMG_CHAIR);
        saveProduct("p-s1-2", "audio-headphone", "s1", "소니 WF-1000XM5 무선이어폰", "소니 플래그십 이어폰. 박스 풀구성.", 280000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.SOLD_OUT, 234, 18, 6, 30, catNameMap, IMG_HEADPHONE);
        saveProduct("p-s1-3", "cookware", "s1", "스노우피크 티타늄 싱글머그 450", "캠핑 필수템. 깨끗하게 사용했습니다.", 45000L, 2500L, ConditionStatus.MINOR_WEAR, SaleStatus.SOLD_OUT, 89, 7, 2, 20, catNameMap, IMG_LAMP);
        saveProduct("p-s1-4", "lens", "s1", "캐논 RF 35mm F1.8 IS STM 렌즈", "RF 마운트 단렌즈. 풍경/인물 모두 좋습니다.", 450000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.RESERVED, 178, 14, 5, 10, catNameMap, IMG_CAMERA);
        saveProduct("p-s1-5", "wearable", "s1", "애플워치 SE 2세대 40mm 미드나이트", "아이폰 바꾸면서 정리합니다.", 180000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.RESERVED, 134, 9, 3, 5, catNameMap, IMG_PHONE);
        saveProduct("p-s1-6", "actioncam", "s1", "고프로 맥스 360도 카메라", "360도 촬영 가능한 액션캠. 여행용으로 구매했다가 거의 못 썼어요.", 320000L, 3000L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 67, 5, 2, 3, catNameMap, IMG_CAMERA);
        saveProduct("p-s1-7", "goods-stray", "s1", "레고 테크닉 포르쉐 911 GT3 RS", "미개봉 새제품. 선물받았는데 조립할 시간이 없네요.", 150000L, 4000L, ConditionStatus.SEALED, SaleStatus.ON_SALE, 45, 3, 1, 1, catNameMap, IMG_ALBUM);

        // ===== s2 IT월드 =====
        saveProduct("p1", "phone-apple", "s2", "아이폰 15 프로 맥스 256GB 자급제", "미개봉 새제품입니다. 자급제.", 1550000L, 0L, ConditionStatus.SEALED, SaleStatus.ON_SALE, 320, 8, 2, 5, catNameMap, IMG_PHONE);
        saveProduct("p2", "phone-samsung", "s2", "갤럭시 S24 울트라 512GB 티타늄블랙", "개봉 후 1회 통화만 했습니다.", 1350000L, 3000L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 180, 6, 1, 4, catNameMap, IMG_PHONE);
        saveProduct("p3", "tablet-ipad", "s2", "아이패드 프로 12.9 M2 256GB", "애플케어 2025년까지.", 1100000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 95, 5, 2, 7, catNameMap, IMG_TABLET);
        saveProduct("p4", "tablet-galaxy", "s2", "갤럭시탭 S9 울트라 256GB", "키보드 커버 포함.", 980000L, 4000L, ConditionStatus.NO_WEAR, SaleStatus.RESERVED, 67, 3, 0, 3, catNameMap, IMG_TABLET);
        saveProduct("p5", "laptop-macbook", "s2", "맥북 프로 14인치 M3 Pro 18GB", "박스 풀구성.", 2800000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 412, 10, 3, 14, catNameMap, IMG_LAPTOP);
        saveProduct("p6", "laptop-macbook", "s2", "맥북 에어 M2 256GB 미드나이트", "가벼운 외출용.", 1150000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 89, 4, 1, 6, catNameMap, IMG_LAPTOP);
        saveProduct("p7", "laptop-common", "s2", "LG 그램 17인치 2024 i7", "업무용 최적화.", 1650000L, 5000L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 56, 2, 0, 2, catNameMap, IMG_LAPTOP);
        saveProduct("p8", "wearable", "s2", "애플워치 울트라2 49mm", "등산용 구매.", 850000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 78, 3, 1, 4, catNameMap, IMG_PHONE);
        saveProduct("p9", "tent", "s2", "코베아 고스트 플러스 텐트", "전자기기 상점이지만 캠핑도 해봅니다.", 450000L, 0L, ConditionStatus.VISIBLE_WEAR, SaleStatus.ON_SALE, 23, 1, 0, 1, catNameMap, IMG_TENT);

        // ===== s3 하이파이클럽 =====
        saveProduct("p10", "audio-headphone", "s3", "소니 WH-1000XM5 무선 헤드폰", "노캔 최강.", 320000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 245, 9, 3, 10, catNameMap, IMG_HEADPHONE);
        saveProduct("p11", "audio-headphone", "s3", "에어팟 맥스 스페이스그레이", "케이스 포함.", 480000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 167, 7, 2, 8, catNameMap, IMG_HEADPHONE);
        saveProduct("p12", "audio-headphone", "s3", "젠하이저 HD660S2", "오픈형 레퍼런스.", 420000L, 4000L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 98, 5, 1, 6, catNameMap, IMG_HEADPHONE);

        // ===== s4 캠핑가자 =====
        saveProduct("p19", "tent", "s4", "스노우피크 랜드록 텐트", "패밀리 캠핑 최고.", 1800000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 356, 10, 3, 15, catNameMap, IMG_TENT);
        saveProduct("p20", "tent-dome", "s4", "힐레베르그 아틀라스 4인용", "4계절 익스페디션.", 2200000L, 0L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 178, 8, 2, 10, catNameMap, IMG_TENT);

        // ===== s6 덕질공간 =====
        saveProduct("p37", "album", "s6", "뉴진스 2nd EP Get Up 미개봉", "한정판 버니.", 35000L, 2000L, ConditionStatus.SEALED, SaleStatus.ON_SALE, 456, 10, 3, 10, catNameMap, IMG_ALBUM);

        // ===== 기타 =====
        saveProduct("p46", "digital-camera", "s7", "소니 A7IV 바디 셔터 1만컷", "풀프레임 미러리스.", 2200000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 178, 6, 2, 9, catNameMap, IMG_CAMERA);
        saveProduct("p48", "guitar-acoustic", "s8", "테일러 214ce 통기타", "ES2 픽업.", 850000L, 0L, ConditionStatus.MINOR_WEAR, SaleStatus.ON_SALE, 89, 4, 1, 6, catNameMap, IMG_AMP);
        saveProduct("p61", "actioncam", "s20", "고프로 히어로12 블랙", "레트로 브이로그용.", 380000L, 3000L, ConditionStatus.NO_WEAR, SaleStatus.ON_SALE, 56, 2, 0, 2, catNameMap, IMG_CAMERA);
    }

    private void saveProduct(
            String pId, String catCode, String sId, String title, String desc,
            Long price, Long shipFee, ConditionStatus condition, SaleStatus saleStatus,
            int view, int like, int comment, int daysAgo,
            Map<String, String> catNameMap, String imageUrl
    ) {
        // 1. 카테고리 & 판매자 조회
        String categoryName = catNameMap.getOrDefault(catCode, "기타");
        Category category = categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
        UUID sellerUuid = sellerMap.get(sId);

        // 2. MySQL 저장 (Entity)
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

        product.addImage(imageUrl); // 썸네일로 사용될 첫 번째 이미지 추가

        Product savedProduct = productRepository.save(product);
        productMap.put(pId, savedProduct);

        // 3. Elasticsearch 저장 (Document)
        // Document의 구조에 맞춰 필드 값을 매핑 및 계산합니다.
        ProductDocument document = ProductDocument.builder()
                .id(String.valueOf(savedProduct.getId())) // ES ID = MySQL PK (String)
                .productUuid(savedProduct.getUuid().toString()) // 외부 노출용 UUID
                .saleSortPriority(calculateSaleSortPriority(savedProduct.getSaleStatus())) // 정렬 우선순위 계산
                .title(savedProduct.getTitle())
                .description(savedProduct.getDescription())
                .sellerUuid(savedProduct.getSellerUuid().toString())
                .categoryId(savedProduct.getCategory().getId().intValue()) // Document는 Integer, Entity는 Long
                .saleStatus(savedProduct.getSaleStatus())
                .visibilityStatus(savedProduct.getVisibilityStatus())
                .price(savedProduct.getPrice())
                .shippingFee(savedProduct.getShippingFee())
                .viewCount(savedProduct.getViewCount())
                .likeCount(savedProduct.getLikeCount())
                .commentCount(savedProduct.getCommentCount())
                .createdAt(savedProduct.getCreatedAt())
                .thumbnailImageUrl(imageUrl) // 방금 추가한 이미지가 썸네일
                .build();

        productSearchRepository.save(document);
    }

    // 정렬 우선순위 계산 로직 (0: 판매중/예약중, 1: 품절/그외)
    private Integer calculateSaleSortPriority(SaleStatus status) {
        if (status == SaleStatus.ON_SALE || status == SaleStatus.RESERVED) {
            return 0;
        }
        return 1;
    }

    private Map<String, String> getCategoryNameMap() {
        Map<String, String> map = new HashMap<>();
        map.put("electronics", "전자기기");
        map.put("smartphone", "스마트폰");
        map.put("phone-apple", "아이폰");
        map.put("phone-samsung", "삼성");
        map.put("phone-etc", "기타");
        map.put("tablet", "태블릿");
        map.put("tablet-ipad", "아이패드");
        map.put("tablet-galaxy", "갤럭시탭");
        map.put("computer", "PC/노트북");
        map.put("laptop-macbook", "맥북");
        map.put("laptop-common", "일반 노트북");
        map.put("pc-parts", "PC부품");
        map.put("etc-electronics", "기타 가전");
        map.put("wearable", "웨어러블");
        map.put("actioncam", "액션캠");
        map.put("camping", "캠핑/레저");
        map.put("tent-group", "텐트/타프");
        map.put("tent", "텐트/타프");
        map.put("tent-dome", "돔/거실형");
        map.put("tent-etc", "기타 텐트");
        map.put("tarp", "타프");
        map.put("camping-furniture", "캠핑가구");
        map.put("table", "테이블");
        map.put("chair", "의자");
        map.put("camping-gear", "캠핑소품");
        map.put("lamp", "랜턴/조명");
        map.put("cookware", "취사용품");
        map.put("sleeping", "침낭/매트");
        map.put("instruments", "악기/음향");
        map.put("string-instruments", "현악기");
        map.put("guitar-elec", "일렉기타");
        map.put("guitar-acoustic", "통기타");
        map.put("guitar-bass", "베이스");
        map.put("audio-gear", "음향기기");
        map.put("audio-headphone", "헤드폰/이어폰");
        map.put("audio-speaker", "스피커");
        map.put("audio-amp", "앰프/DAC");
        map.put("audio-dac", "앰프/DAC");
        map.put("keyboard-instruments", "건반악기");
        map.put("piano", "피아노/신디");
        map.put("camera", "카메라/렌즈");
        map.put("digital-camera", "디지털 카메라");
        map.put("dslr-mirrorless", "DSLR/미러리스");
        map.put("compact-camera", "하이엔드/컴팩트");
        map.put("camera-parts", "렌즈/주변기기");
        map.put("lens", "교환렌즈");
        map.put("camera-acc", "삼각대/액세서리");
        map.put("film-group", "필름카메라");
        map.put("film-camera", "필름 바디");
        map.put("body", "DSLR/미러리스");
        map.put("golf", "골프");
        map.put("golf-club", "골프채");
        map.put("golf-driver", "드라이버");
        map.put("golf-wood", "우드/유틸");
        map.put("golf-iron", "아이언");
        map.put("golf-wedge", "웨지");
        map.put("golf-putter", "퍼터");
        map.put("golf-acc-group", "용품/의류");
        map.put("golf-bag", "골프백");
        map.put("golf-wear", "골프웨어");
        map.put("golf-acc", "기타용품");
        map.put("goods", "스타굿즈");
        map.put("idol-boy", "보이그룹");
        map.put("goods-bts", "BTS");
        map.put("goods-seventeen", "세븐틴");
        map.put("goods-stray", "스트레이키즈");
        map.put("idol-girl", "걸그룹");
        map.put("goods-newjeans", "뉴진스");
        map.put("goods-ive", "IVE");
        map.put("goods-aespa", "aespa");
        map.put("goods-common", "교통수단/일반");
        map.put("album", "앨범");
        map.put("photocard", "포토카드");
        map.put("concert", "콘서트티켓");
        return map;
    }
}