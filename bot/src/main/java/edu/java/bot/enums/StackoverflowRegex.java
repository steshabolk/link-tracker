package edu.java.bot.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum StackoverflowRegex implements SourceRegex {

    QUESTION("/questions/(?<id>[\\d]+)");

    private final String regex;

    @Override
    public String regex() {
        return regex;
    }
}
