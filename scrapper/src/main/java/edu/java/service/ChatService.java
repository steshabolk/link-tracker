package edu.java.service;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.entity.Chat;
import edu.java.exception.ApiExceptionType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {

    private final Map<Long, Chat> chats = new HashMap<>();

    public Chat getChat(Long chatId) {
        if (!chats.containsKey(chatId)) {
            throw ApiExceptionType.CHAT_NOT_FOUND.toException(chatId);
        }
        return chats.get(chatId);
    }

    public void registerChat(Long chatId) {
        if (chats.containsKey(chatId)) {
            throw ApiExceptionType.CHAT_ALREADY_EXISTS.toException(chatId);
        }
        Chat chat = Chat.builder().id((long) chats.size()).chatId(chatId).build();
        chats.put(chatId, chat);
        log.debug("new chat{chatId={}} was registered", chatId);
    }

    public void deleteChat(Long chatId) {
        if (!chats.containsKey(chatId)) {
            throw ApiExceptionType.CHAT_NOT_FOUND.toException(chatId);
        }
        chats.remove(chatId);
        log.debug("chat{chatId={}} was deleted", chatId);
    }

    public ListLinksResponse getLinks(Long chatId) {
        Chat chat = getChat(chatId);
        List<LinkResponse> trackedLinks = chat.getLinks().stream()
            .map(link -> new LinkResponse(link.getId(), URI.create(link.getUrl())))
            .toList();
        return new ListLinksResponse(trackedLinks, trackedLinks.size());
    }
}
