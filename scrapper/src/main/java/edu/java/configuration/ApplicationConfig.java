package edu.java.configuration;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Validated
@EnableScheduling
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(
    @NotNull
    LinkUpdaterScheduler linkUpdaterScheduler,
    @NotNull
    GithubClient githubClient,
    @NotNull
    StackoverflowClient stackoverflowClient
) {
    public record LinkUpdaterScheduler(boolean enable, @NotNull Duration interval, @NotNull Duration forceCheckDelay) {
    }

    public record GithubClient(@DefaultValue("https://api.github.com") String api) {
    }

    public record StackoverflowClient(@DefaultValue("https://api.stackexchange.com/2.3") String api) {
    }
}
