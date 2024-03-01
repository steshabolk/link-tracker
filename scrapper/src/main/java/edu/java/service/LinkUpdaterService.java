package edu.java.service;

import edu.java.enums.LinkType;
import edu.java.handler.LinkUpdateHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LinkUpdaterService {

    private final LinkService linkService;
    private final List<LinkUpdateHandler> linkUpdateHandlers;

    public void updateLinks() {
        linkService.getActiveLinks().forEach(link -> {
            LinkType linkType = link.getLinkType();
            linkUpdateHandlers.stream()
                .filter(handler -> handler.getLinkType().equals(linkType))
                .findFirst()
                .ifPresentOrElse(
                    handler -> handler.updateLink(link),
                    () -> log.warn("handler for LinkType={} was not found", linkType)
                );
        });
    }
}
