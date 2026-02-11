package dukku.deposit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableResilientMethods
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {
        "dukku.deposit", // TODO: semicolon에서 deposit로 변경 완료
        "dukku.common"
})
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
