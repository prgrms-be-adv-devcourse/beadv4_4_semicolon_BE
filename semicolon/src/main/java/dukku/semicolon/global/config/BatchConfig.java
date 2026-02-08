package dukku.semicolon.global.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository
public class BatchConfig {


    @Bean
    public DataSourceInitializer postgresBatchInitializer(DataSource dataSource) {
        return initializer(
                dataSource,
                "org/springframework/batch/core/schema-postgresql.sql"
        );
    }

    /**
     * 공통 Initializer 생성 로직
     */
    private DataSourceInitializer initializer(DataSource dataSource, String script) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource(script));
        populator.setContinueOnError(true); // 이미 테이블이 있어도 무시

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
