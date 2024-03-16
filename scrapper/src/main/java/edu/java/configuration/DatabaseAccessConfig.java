package edu.java.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
@SuppressWarnings("HideUtilityClassConstructor")
public class DatabaseAccessConfig {

    @Bean
    public static DynamicBeanDefinitionRegistrar beanDefinitionRegistrar(Environment environment) {
        String databaseAccessType = environment.getProperty("app.database-access-type");
        log.debug("database access: {}", databaseAccessType);
        return new DynamicBeanDefinitionRegistrar(environment,
            "app.database-access-type-beans." + databaseAccessType);
    }
}
