package edu.java.util;

import edu.java.configuration.ApplicationConfig;
import edu.java.enums.LinkType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkTypeUtil {

    private static final List<LinkType> DOMAINS = Arrays.stream(LinkType.values()).toList();
    private static Map<LinkType, List<String>> sourceRegex;

    private LinkTypeUtil(ApplicationConfig applicationConfig) {
        sourceRegex = applicationConfig.sourceRegex().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().values().stream().toList()));
    }

    public static Optional<LinkType> getLinkType(String host, String url) {
        return DOMAINS.stream()
            .filter(it -> it.getDomain().equals(host))
            .findFirst()
            .filter(it -> isSupportedSource(it, url.substring(url.indexOf(it.getDomain()))));
    }

    private static boolean isSupportedSource(LinkType linkType, String url) {
        return Optional.ofNullable(sourceRegex.get(linkType))
            .map(it -> it.stream()
                .anyMatch(regex -> Pattern.matches(linkType.getDomain() + regex, url)))
            .orElse(false);
    }
}
