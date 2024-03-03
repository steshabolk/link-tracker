package edu.java.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GithubRegex implements SourceRegex {

    REPOSITORY("/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)"),
    BRANCH("/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/tree/(?<branch>[\\w-\\./]+)"),
    PULL_REQUEST("/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/pull/(?<num>\\d+)"),
    ISSUE("/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)");

    private final String regex;

    @Override
    public String regex() {
        return regex;
    }
}
