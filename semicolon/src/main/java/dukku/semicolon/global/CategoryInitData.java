package dukku.semicolon.global;

import dukku.semicolon.boundedContext.product.entity.Category;
import dukku.semicolon.boundedContext.product.out.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Order(1)
public class CategoryInitData {

    @Bean
    public CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                // 데이터가 이미 존재하면 초기화하지 않음
                if (categoryRepository.count() > 0) {
                    return;
                }

                // ==========================================
                // 1. 전자기기 (Mock ID: electronics)
                // ==========================================
                Category electronics = categoryRepository.save(Category.createRoot("전자기기"));

                // 1-1. 스마트폰
                Category smartphone = categoryRepository.save(Category.createChild("스마트폰", electronics));
                categoryRepository.save(Category.createChild("아이폰", smartphone));
                categoryRepository.save(Category.createChild("삼성", smartphone));
                categoryRepository.save(Category.createChild("기타", smartphone));

                // 1-2. 태블릿
                Category tablet = categoryRepository.save(Category.createChild("태블릿", electronics));
                categoryRepository.save(Category.createChild("아이패드", tablet));
                categoryRepository.save(Category.createChild("갤럭시탭", tablet));

                // 1-3. PC/노트북
                Category computer = categoryRepository.save(Category.createChild("PC/노트북", electronics));
                categoryRepository.save(Category.createChild("맥북", computer));
                categoryRepository.save(Category.createChild("일반 노트북", computer));
                categoryRepository.save(Category.createChild("PC부품", computer));

                // 1-4. 기타 가전
                Category etcElec = categoryRepository.save(Category.createChild("기타 가전", electronics));
                categoryRepository.save(Category.createChild("웨어러블", etcElec));
                categoryRepository.save(Category.createChild("액션캠", etcElec));


                // ==========================================
                // 2. 캠핑/레저 (Mock ID: camping)
                // ==========================================
                Category camping = categoryRepository.save(Category.createRoot("캠핑/레저"));

                // 2-1. 텐트/타프
                Category tentGroup = categoryRepository.save(Category.createChild("텐트/타프", camping));
                categoryRepository.save(Category.createChild("돔/거실형", tentGroup));
                categoryRepository.save(Category.createChild("기타 텐트", tentGroup));
                categoryRepository.save(Category.createChild("타프", tentGroup));

                // 2-2. 캠핑가구
                Category campingFurniture = categoryRepository.save(Category.createChild("캠핑가구", camping));
                categoryRepository.save(Category.createChild("테이블", campingFurniture));
                categoryRepository.save(Category.createChild("의자", campingFurniture));

                // 2-3. 캠핑소품
                Category campingGear = categoryRepository.save(Category.createChild("캠핑소품", camping));
                categoryRepository.save(Category.createChild("랜턴/조명", campingGear));
                categoryRepository.save(Category.createChild("취사용품", campingGear));
                categoryRepository.save(Category.createChild("침낭/매트", campingGear));


                // ==========================================
                // 3. 악기/음향 (Mock ID: instruments)
                // ==========================================
                Category instruments = categoryRepository.save(Category.createRoot("악기/음향"));

                // 3-1. 현악기
                Category strings = categoryRepository.save(Category.createChild("현악기", instruments));
                categoryRepository.save(Category.createChild("일렉기타", strings));
                categoryRepository.save(Category.createChild("통기타", strings));
                categoryRepository.save(Category.createChild("베이스", strings));

                // 3-2. 음향기기
                Category audioGear = categoryRepository.save(Category.createChild("음향기기", instruments));
                categoryRepository.save(Category.createChild("헤드폰/이어폰", audioGear));
                categoryRepository.save(Category.createChild("스피커", audioGear));
                categoryRepository.save(Category.createChild("앰프/DAC", audioGear));

                // 3-3. 건반악기
                Category keyboards = categoryRepository.save(Category.createChild("건반악기", instruments));
                categoryRepository.save(Category.createChild("피아노/신디", keyboards));


                // ==========================================
                // 4. 카메라/렌즈 (Mock ID: camera)
                // ==========================================
                Category camera = categoryRepository.save(Category.createRoot("카메라/렌즈"));

                // 4-1. 디지털 카메라
                Category digitalCam = categoryRepository.save(Category.createChild("디지털 카메라", camera));
                categoryRepository.save(Category.createChild("DSLR/미러리스", digitalCam));
                categoryRepository.save(Category.createChild("하이엔드/컴팩트", digitalCam));

                // 4-2. 렌즈/주변기기
                Category cameraParts = categoryRepository.save(Category.createChild("렌즈/주변기기", camera));
                categoryRepository.save(Category.createChild("교환렌즈", cameraParts));
                categoryRepository.save(Category.createChild("삼각대/액세서리", cameraParts));

                // 4-3. 필름카메라
                Category filmGroup = categoryRepository.save(Category.createChild("필름카메라", camera));
                categoryRepository.save(Category.createChild("필름 바디", filmGroup));


                // ==========================================
                // 5. 골프 (Mock ID: golf)
                // ==========================================
                Category golf = categoryRepository.save(Category.createRoot("골프"));

                // 5-1. 골프채
                Category golfClub = categoryRepository.save(Category.createChild("골프채", golf));
                categoryRepository.save(Category.createChild("드라이버", golfClub));
                categoryRepository.save(Category.createChild("우드/유틸", golfClub));
                categoryRepository.save(Category.createChild("아이언", golfClub));
                categoryRepository.save(Category.createChild("웨지", golfClub));
                categoryRepository.save(Category.createChild("퍼터", golfClub));

                // 5-2. 용품/의류
                Category golfAccGroup = categoryRepository.save(Category.createChild("용품/의류", golf));
                categoryRepository.save(Category.createChild("골프백", golfAccGroup));
                categoryRepository.save(Category.createChild("골프웨어", golfAccGroup));
                categoryRepository.save(Category.createChild("기타용품", golfAccGroup));


                // ==========================================
                // 6. 스타굿즈 (Mock ID: goods)
                // ==========================================
                Category goods = categoryRepository.save(Category.createRoot("스타굿즈"));

                // 6-1. 보이그룹
                Category idolBoy = categoryRepository.save(Category.createChild("보이그룹", goods));
                categoryRepository.save(Category.createChild("BTS", idolBoy));
                categoryRepository.save(Category.createChild("세븐틴", idolBoy));
                categoryRepository.save(Category.createChild("스트레이키즈", idolBoy));

                // 6-2. 걸그룹
                Category idolGirl = categoryRepository.save(Category.createChild("걸그룹", goods));
                categoryRepository.save(Category.createChild("뉴진스", idolGirl));
                categoryRepository.save(Category.createChild("IVE", idolGirl));
                categoryRepository.save(Category.createChild("aespa", idolGirl));

                // 6-3. 교통수단/일반
                Category goodsCommon = categoryRepository.save(Category.createChild("교통수단/일반", goods));
                categoryRepository.save(Category.createChild("앨범", goodsCommon));
                categoryRepository.save(Category.createChild("포토카드", goodsCommon));
                categoryRepository.save(Category.createChild("콘서트티켓", goodsCommon));
            }
        };
    }
}