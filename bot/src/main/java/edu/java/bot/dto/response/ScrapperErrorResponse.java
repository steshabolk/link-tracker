package edu.java.bot.dto.response;

import java.util.List;

public record ScrapperErrorResponse(String description, String code, String exceptionName, String exceptionMessage,
                                    List<String> stacktrace) {
}
