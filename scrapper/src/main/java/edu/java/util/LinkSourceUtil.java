package edu.java.util;

import edu.java.configuration.ApplicationConfig;
import edu.java.enums.LinkType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkSourceUtil {

    private static ApplicationConfig applicationConfig;
    private static Map<LinkType, String> domains;
    private static Map<LinkType, List<String>> regex;

    public LinkSourceUtil(ApplicationConfig applicationConfig) {
        LinkSourceUtil.applicationConfig = applicationConfig;
        domains = initDomainMap(applicationConfig);
        regex = initRegexMap(applicationConfig);
    }

    public static Optional<ApplicationConfig.LinkSource> getLinkSource(LinkType linkType) {
        return Optional.ofNullable(applicationConfig.linkSources().get(linkType));
    }

    public static String getDomain(LinkType linkType) {
        return domains.get(linkType);
    }

    public static Optional<LinkType> getLinkType(String host, String url) {
        return domains.entrySet().stream()
            .filter(it -> it.getValue().equals(host))
            .map(Map.Entry::getKey)
            .filter(it -> isSupportedSource(it, url.substring(url.indexOf(domains.get(it)))))
            .findFirst();
    }

    private static boolean isSupportedSource(LinkType linkType, String url) {
        return regex.get(linkType).stream()
            .anyMatch(it -> Pattern.matches(domains.get(linkType) + it, url));
    }

    private Map<LinkType, List<String>> initRegexMap(ApplicationConfig applicationConfig) {
        return applicationConfig.linkSources().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().handlers().values().stream()
                    .map(ApplicationConfig.LinkSourceHandler::regex)
                    .toList()
            ));
    }

    private Map<LinkType, String> initDomainMap(ApplicationConfig applicationConfig) {
        return applicationConfig.linkSources().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().domain()
            ));
    }
}
