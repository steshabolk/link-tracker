package edu.java.bot.enums;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {

    GITHUB("github.com", Arrays.stream(GithubRegex.values()).toList()),
    STACKOVERFLOW("stackoverflow.com", Arrays.stream(StackoverflowRegex.values()).toList());

    private final String domain;
    private final List<? extends Enum<? extends SourceRegex>> sources;

    public String getCommandBulletPoint() {
        return String.format("%s *%s:*\n%s", Emoji.CHECK.getMarkdown(), domain,
            sources.stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .map(it -> it.replace("_", " "))
                .map(it -> "      - " + it)
                .collect(Collectors.joining("\n"))
        );
    }

    public boolean isSupportedSource(String url) {
        return sources.stream()
            .map(e -> (SourceRegex) e)
            .map(SourceRegex::regex)
            .anyMatch(regex -> Pattern.matches(domain + regex, url));
    }
}
