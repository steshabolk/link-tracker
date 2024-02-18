package edu.java.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    GITHUB("github.com"),
    STACKOVERFLOW("stackoverflow.com");

    private final String domain;
}
