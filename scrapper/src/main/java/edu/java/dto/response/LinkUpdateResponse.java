package edu.java.dto.response;

import java.util.Set;

public record LinkUpdateResponse(String url, String message, Set<Long> chats) {
}
