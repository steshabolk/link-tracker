package edu.java.enums;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    GITHUB("github.com", Arrays.stream(GithubRegex.values()).toList()),
    STACKOVERFLOW("stackoverflow.com", Arrays.stream(StackoverflowRegex.values()).toList());

    private final String domain;
    private final List<? extends SourceRegex> sourceRegex;

    public boolean isSupportedSource(String url) {
        return sourceRegex.stream()
            .map(SourceRegex::regex)
            .anyMatch(regex -> Pattern.matches(domain + regex, url));
    }
}
