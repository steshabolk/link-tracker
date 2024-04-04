package edu.java.bot.configuration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotEmpty
    String telegramToken,
    @NotNull
    ScrapperClientConfig scrapperClient,
    Map<String, LinkSource> linkSources
) {

    public record ScrapperClientConfig(@NotNull String api, RetryConfig retry) {
    }

    public record RetryConfig(@NotNull RetryStrategy strategy, @NotNull Integer maxAttempts,
                              @NotNull Duration backoff, Duration maxBackoff, @NotEmpty List<Integer> codes) {
    }

    public record LinkSource(@NotNull String domain, Map<String, String> regex) {
    }

    public enum RetryStrategy {
        FIXED, LINEAR, EXPONENTIAL
    }
}
