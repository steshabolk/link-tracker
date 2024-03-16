package edu.java.bot.configuration;

import edu.java.bot.enums.LinkType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    Map<LinkType, Map<String, String>> sourceRegex
) {

    public record ScrapperClientConfig(@NotNull String api) {
    }
}
