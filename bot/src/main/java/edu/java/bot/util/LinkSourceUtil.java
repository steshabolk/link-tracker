package edu.java.bot.util;

import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.enums.Emoji;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkSourceUtil {

    @Getter
    private static String availableSourcesDescription;
    private static ApplicationConfig applicationConfig;

    public LinkSourceUtil(ApplicationConfig applicationConfig) {
        LinkSourceUtil.applicationConfig = applicationConfig;
        availableSourcesDescription = buildAvailableSourcesDescription(applicationConfig);
    }

    public static Optional<String> getLinkType(String host) {
        return applicationConfig.linkSources().entrySet().stream()
            .filter(it -> it.getValue().domain().equals(host))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    public static boolean isSupportedSource(String host, String url) {
        return getLinkType(host)
            .map(it -> applicationConfig.linkSources().get(it))
            .map(it -> checkLinkPattern(it, url.substring(url.indexOf(it.domain()))))
            .orElse(false);
    }

    private static boolean checkLinkPattern(ApplicationConfig.LinkSource linkSource, String url) {
        return linkSource.regex().values().stream()
            .anyMatch(it -> Pattern.matches(linkSource.domain() + it, url));
    }

    private String buildAvailableSourcesDescription(ApplicationConfig applicationConfig) {
        return applicationConfig.linkSources().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> getLinkSourceBulletPoint(e.getValue()))
            .collect(Collectors.joining("\n"));
    }

    private String getLinkSourceBulletPoint(ApplicationConfig.LinkSource linkSource) {
        return String.format("%s %s:\n%s", Emoji.CHECK.toUnicode(), TextUtil.toBold(linkSource.domain()),
            linkSource.regex().keySet().stream()
                .sorted()
                .map(it -> it.replace("-", " "))
                .map(it -> "      - " + it)
                .collect(Collectors.joining("\n"))
        );
    }
}
