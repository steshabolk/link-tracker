package edu.java.service;

import edu.java.dto.response.LinkUpdateResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BotService {

    public void sendLinkUpdate(Link link, String message) {
        LinkUpdateResponse response = new LinkUpdateResponse(
            link.getUrl(),
            message,
            link.getChats().stream()
                .map(Chat::getChatId)
                .collect(Collectors.toSet())
        );
        log.debug("send an update to the bot:\nurl={}\nmessage={}", response.url(), response.message());
    }
}
