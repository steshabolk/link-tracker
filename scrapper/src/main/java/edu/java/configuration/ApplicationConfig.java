package edu.java.configuration;

import edu.java.enums.LinkType;
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
    Map<String, List<String>> databaseAccessTypeBeans,
    @NotNull
    Integer linkAge,
    @NotNull
    LinkUpdaterScheduler linkUpdaterScheduler,
    @NotNull
    GithubClient githubClient,
    @NotNull
    StackoverflowClient stackoverflowClient,
    @NotNull
    BotClient botClient,
    Map<LinkType, Map<String, String>> sourceRegex
) {
    public record LinkUpdaterScheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }

    public record GithubClient(@DefaultValue("https://api.github.com") String api) {
    }

    public record StackoverflowClient(@DefaultValue("https://api.stackexchange.com/2.3") String api) {
    }

    public record BotClient(@NotNull String api) {
    }

    public enum AccessType {
        JDBC, JOOQ, JPA
    }
}
