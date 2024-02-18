package edu.java.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LinkSourceException extends RuntimeException {

    private final String message;
    private final String code;
}
