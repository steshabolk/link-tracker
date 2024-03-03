package edu.java.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ScrapperExceptionCode {

    INVALID_LINK,
    NOT_SUPPORTED_SOURCE,
    CHAT_NOT_FOUND,
    LINK_NOT_FOUND,
    CHAT_ALREADY_EXISTS,
    LINK_ALREADY_EXISTS;
}
