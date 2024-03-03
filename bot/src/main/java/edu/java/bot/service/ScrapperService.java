package edu.java.bot.service;

import edu.java.bot.client.ScrapperClient;
import edu.java.bot.dto.request.AddLinkRequest;
import edu.java.bot.dto.request.RemoveLinkRequest;
import edu.java.bot.dto.response.LinkResponse;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScrapperService {

    private final ScrapperClient scrapperClient;

    public void registerChat(Long chatId) {
        log.debug("register chat={}", chatId);
        try {
            scrapperClient.registerChat(chatId);
        } catch (RuntimeException ex) {
            logClientException(ex);
        }
    }

    public void deleteChat(Long chatId) {
        log.debug("delete chat={}", chatId);
        try {
            scrapperClient.deleteChat(chatId);
        } catch (RuntimeException ex) {
            logClientException(ex);
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

    private void logClientException(RuntimeException ex) {
        log.info("client error: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc
            && ArrayUtils.isNotEmpty(clientExc.getResponseBodyAsByteArray())) {
            log.info("response: {}", clientExc.getResponseBodyAsString());
        }
    }
}
