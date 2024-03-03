package edu.java.dto.response;

import java.net.URI;
import java.util.List;

public record LinkUpdateResponse(Long id, URI url, String description, List<Long> tgChatIds) {
}
