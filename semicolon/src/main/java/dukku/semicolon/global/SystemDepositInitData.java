package dukku.semicolon.global;

import dukku.semicolon.boundedContext.deposit.app.DepositFacade;
import dukku.semicolon.boundedContext.user.app.user.UserFacade;
import dukku.semicolon.boundedContext.user.app.user.UserSupport;
import dukku.semicolon.boundedContext.user.entity.User;
import dukku.semicolon.boundedContext.user.entity.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시스템 예치금 관리를 위한 admin 계정 생성
 * User-Deposit 1:1 대응이므로 계정 생성 시 Deposit 함께 생성
 * 일단 임시로 초기화 코드를 이용해 밀어넣는 방식으로 구현, 추후 Admin을 타고 관리하는 방식으로 변경 고려
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(3) // 다른 2개의 초기화 코드가 실행된 후 실행
public class SystemDepositInitData {

    private static final String SYSTEM_DEPOSIT_EMAIL = "admin-deposit@dukku.shop";
    private static final String SYSTEM_DEPOSIT_NICKNAME = "시스템-예치금";
    // 임시 하드코딩 UUID (추후 API Client 등으로 대체 예정)
    public static final UUID SYSTEM_USER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Long INITIAL_CAPITAL = 1_000_000_000L; // 10억

    @Bean
    public CommandLineRunner initSystemDeposit(
            UserFacade userFacade,
            UserSupport userSupport,
            DepositFacade depositFacade,
            Environment env,
            RedisTemplate<String, Object> redisTemplate) {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                // 1. 시스템 계정 존재 여부 확인 (없으면 복구or생성)
                if (userSupport.findByEmail(SYSTEM_DEPOSIT_EMAIL).isPresent()) {
                    log.info("[SystemDepositInitData] 시스템 예치금 계정이 이미 존재합니다.");
                    // 이미 존재하더라도 UUID가 다르면 문제될 수 있으나, 일단은 스킵
                } else {
                    // 비밀번호 주입
                    String password = env.getProperty("system.admin-deposit.password");

                    // Redis에 미리 인증 완료 상태로 세팅
                    redisTemplate.opsForValue().set("email:verify:ok:" + SYSTEM_DEPOSIT_EMAIL, "true",
                            java.time.Duration.ofMinutes(5));

                    // 시스템 계정 생성
                    String encodedPassword = userSupport.encode(password);

                    // User 객체 생성 시 UUID를 직접 주입
                    // API 도입되면 이 부분은 삭제하고 API로 처리
                    User systemUser = User.builder()
                            .email(SYSTEM_DEPOSIT_EMAIL)
                            .password(encodedPassword)
                            .role(Role.SYSTEM)
                            .nickname(SYSTEM_DEPOSIT_NICKNAME)
                            .uuid(SYSTEM_USER_UUID) // SourceUser의 필드
                            .build();

                    User savedUser = userSupport.save(systemUser);
                    log.info("[SystemDepositInitData] 시스템 예치금 계정 생성 완료: {} (UUID={})", SYSTEM_DEPOSIT_EMAIL,
                            savedUser.getUuid());
                }

                // 2. 시스템 예치금 계좌 확인 및 초기 자본금 주입
                // findDeposit 호출 시 Deposit이 없으면 생성됨 (Lazy)
                if (depositFacade.findDeposit(SYSTEM_USER_UUID).getBalance() == 0L) {
                    depositFacade.injectSystemCapital(SYSTEM_USER_UUID, INITIAL_CAPITAL);
                    log.info("[SystemDepositInitData] 시스템 예치금 초기 자본금 납입 완료: {} KRW", INITIAL_CAPITAL);
                } else {
                    log.info("[SystemDepositInitData] 시스템 예치금 잔액이 이미 존재하여 자본금 납입을 건너뜁니다.");
                }
            }
        };
    }
}
