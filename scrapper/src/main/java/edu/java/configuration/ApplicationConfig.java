package edu.java.configuration;

import edu.java.enums.LinkType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Validated
@EnableScheduling
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull
    AccessType databaseAccessType,
    @NotNull
    Integer linkAge,
    @NotNull
    Integer linkUpdateBatchSize,
    @NotNull
    LinkUpdaterScheduler linkUpdaterScheduler,
    @NotNull
    GithubClient githubClient,
    @NotNull
    StackoverflowClient stackoverflowClient,
    @NotNull
    BotClient botClient,
    Map<LinkType, LinkSource> linkSources
) {
    public record LinkUpdaterScheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }

    public record GithubClient(@DefaultValue("https://api.github.com") String api, RetryConfig retry) {
    }

    public record StackoverflowClient(@DefaultValue("https://api.stackexchange.com/2.3") String api, RetryConfig retry) {
    }

    public record BotClient(@NotNull String api, RetryConfig retry) {
    }

    public record RetryConfig(@NotNull RetryStrategy strategy, @NotNull Integer maxAttempts,
                              @NotNull Duration backoff, Duration maxBackoff, @NotEmpty List<Integer> codes) {
    }

    public record LinkSource(@NotEmpty String domain, Map<String, LinkSourceHandler> handlers) {
    }

    public record LinkSourceHandler(@NotEmpty String regex, @NotEmpty String handler) {
    }

    public enum AccessType {
        JDBC, JOOQ, JPA
    }

    public enum RetryStrategy {
        FIXED, LINEAR, EXPONENTIAL
    }
}
