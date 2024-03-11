package edu.java.bot.service;

import edu.java.bot.client.ScrapperClient;
import edu.java.bot.dto.request.AddLinkRequest;
import edu.java.bot.dto.request.RemoveLinkRequest;
import edu.java.bot.dto.response.LinkResponse;
import edu.java.bot.handler.ClientExceptionHandler;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScrapperService {

    private final ScrapperClient scrapperClient;
    private final ClientExceptionHandler clientExceptionHandler;

    public void registerChat(Long chatId) {
        log.debug("register chat={}", chatId);
        try {
            scrapperClient.registerChat(chatId);
        } catch (RuntimeException ex) {
            clientExceptionHandler.handleClientResponse(ex);
        }
    }

    public void deleteChat(Long chatId) {
        log.debug("delete chat={}", chatId);
        try {
            scrapperClient.deleteChat(chatId);
        } catch (RuntimeException ex) {
            clientExceptionHandler.handleClientResponse(ex);
        }
    }

    public void addLink(Long chatId, URI link) {
        log.debug("chat={}: add link={}", chatId, link);
        scrapperClient.addLink(chatId, new AddLinkRequest(link));
    }

    public void removeLink(Long chatId, URI link) {
        log.debug("chat={}: remove link={}", chatId, link);
        scrapperClient.removeLink(chatId, new RemoveLinkRequest(link));
    }

    public List<URI> getLinks(Long chatId) {
        log.debug("get links for the chat={}", chatId);
        return scrapperClient.getLinks(chatId).links()
            .stream()
            .map(LinkResponse::url)
            .toList();
    }
}
