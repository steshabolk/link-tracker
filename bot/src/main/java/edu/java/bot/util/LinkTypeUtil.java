package edu.java.bot.util;

import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkTypeUtil {

    private static final List<LinkType> DOMAINS = Arrays.stream(LinkType.values()).toList();
    private static Map<LinkType, List<String>> sourceRegex;
    @Getter
    private static String availableSourcesDescription;

    private LinkTypeUtil(ApplicationConfig applicationConfig) {
        sourceRegex = applicationConfig.sourceRegex().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().values().stream().toList()));
        availableSourcesDescription = applicationConfig.sourceRegex().entrySet().stream()
            .map(e -> getLinkTypeBulletPoint(e.getKey(), e.getValue().keySet()))
            .collect(Collectors.joining("\n"));
    }

    public static Optional<LinkType> getLinkType(String host) {
        return DOMAINS.stream()
            .filter(it -> it.getDomain().equals(host))
            .findFirst();
    }

    public static boolean isSupportedSource(String host, String url) {
        return getLinkType(host)
            .map(it -> checkLinkPattern(it, url.substring(url.indexOf(it.getDomain()))))
            .orElse(false);
    }

    private static boolean checkLinkPattern(LinkType linkType, String url) {
        return Optional.ofNullable(sourceRegex.get(linkType))
            .map(it -> it.stream()
                .anyMatch(regex -> Pattern.matches(linkType.getDomain() + regex, url)))
            .orElse(false);
    }

    private String getLinkTypeBulletPoint(LinkType linkType, Set<String> linkValues) {
        return String.format("%s *%s:*\n%s", Emoji.CHECK.getMarkdown(), linkType.getDomain(),
            linkValues.stream()
                .map(it -> it.replace("-", " "))
                .map(it -> "      - " + it)
                .collect(Collectors.joining("\n"))
        );
    }
}
