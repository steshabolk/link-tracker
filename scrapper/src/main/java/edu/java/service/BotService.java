package edu.java.service;

import edu.java.client.BotClient;
import edu.java.dto.response.LinkUpdateResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Service
public class BotService {

    private final BotClient botClient;

    public boolean sendLinkUpdate(Link link, String message) {
        LinkUpdateResponse linkUpdate = new LinkUpdateResponse(
            link.getId(),
            URI.create(link.getUrl()),
            message,
            link.getChats().stream()
                .map(Chat::getChatId)
                .collect(Collectors.toList())
        );
        log.debug(
            "send link update to the bot client: id={}\nurl={}\nmessage={}",
            linkUpdate.id(),
            linkUpdate.url(),
            linkUpdate.description()
        );
        try {
            botClient.postUpdate(linkUpdate);
            return true;
        } catch (RuntimeException ex) {
            log.info("client error when sending an update: {}", ex.getMessage());
            if (ex instanceof WebClientResponseException clientExc
                && ArrayUtils.isNotEmpty(clientExc.getResponseBodyAsByteArray())) {
                log.info("response: {}", clientExc.getResponseBodyAsString());
            }
            return false;
        }
    }
}
