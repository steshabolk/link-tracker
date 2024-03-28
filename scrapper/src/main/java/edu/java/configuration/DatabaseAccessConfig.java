package edu.java.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DatabaseAccessConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
    public static class JdbcAccessConfig {
        @PostConstruct
        private void init() {
            log.info("database access: jdbc");
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jooq")
    public static class JooqAccessConfig {
        @PostConstruct
        private void init() {
            log.info("database access: jooq");
        }
    }
}
