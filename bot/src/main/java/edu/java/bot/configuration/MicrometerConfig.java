package edu.java.bot.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class MicrometerConfig {

    private final MeterRegistry meterRegistry;

    @Bean
    public Counter messageCounter() {
        return Counter.builder("messages_processed")
            .description("Total number of processed messages")
            .register(meterRegistry);
    }
}
