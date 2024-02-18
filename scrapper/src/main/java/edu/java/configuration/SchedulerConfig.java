package edu.java.configuration;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = {"app.scheduler.enable"}, havingValue = "true")
public class SchedulerConfig {

    private final ApplicationConfig applicationConfig;

    @Autowired
    public SchedulerConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Bean
    public Duration linkUpdateInterval() {
        return applicationConfig.scheduler().interval();
    }

    @Bean
    public Duration linkUpdateForceDelay() {
        return applicationConfig.scheduler().forceCheckDelay();
    }
}
