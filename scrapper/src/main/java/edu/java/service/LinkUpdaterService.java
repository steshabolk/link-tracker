package edu.java.service;

import edu.java.configuration.ApplicationConfig;
import edu.java.dto.response.LinkUpdate;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.handler.LinkUpdateHandler;
import edu.java.service.sender.UpdateSender;
import edu.java.util.LinkSourceUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class LinkUpdaterService {

    @Value("${app.link-update-batch-size}")
    private Integer batchSize;
    @Value("${app.link-age}")
    private Integer linkAgeInMinutes;
    private final LinkService linkService;
    private final UpdateSender updateSender;
    private final Map<String, LinkUpdateHandler> linkUpdateHandlers;

    public LinkUpdaterService(
        LinkService linkService, UpdateSender updateSender,
        List<LinkUpdateHandler> linkUpdateHandlers
    ) {
        this.linkService = linkService;
        this.updateSender = updateSender;
        this.linkUpdateHandlers =
            linkUpdateHandlers.stream()
                .collect(Collectors.toMap(
                    it -> it.getClass().getCanonicalName(),
                    Function.identity()
                ));
    }

    @Transactional
    public void updateLinks() {
        linkService.getLinksToUpdate(linkAgeInMinutes, batchSize)
            .forEach(this::processLinkUpdate);
    }

    private void processLinkUpdate(Link link) {
        Optional<LinkUpdateHandler> handler = LinkSourceUtil.getLinkSource(link.getLinkType())
            .flatMap(it -> getLinkUpdateHandler(link, it));
        if (handler.isEmpty()) {
            log.warn("no update handler: LinkType={}, link=[{}]", link.getLinkType(), link.getUrl());
            return;
        }
        OffsetDateTime checkedAt = OffsetDateTime.now();
        try {
            handler.get().getLinkUpdate(link)
                .ifPresentOrElse(
                    it -> notifyBot(link, it, checkedAt),
                    () -> linkService.updateCheckedAt(link, checkedAt)
                );
        } catch (RuntimeException ex) {
            handleClientExceptionOnLinkUpdate(ex, link);
        }
    }

    private Optional<LinkUpdateHandler> getLinkUpdateHandler(Link link, ApplicationConfig.LinkSource linkSource) {
        return linkSource.handlers().values().stream()
            .filter(it -> Pattern.matches("https://" + linkSource.domain() + it.regex(), link.getUrl()))
            .map(ApplicationConfig.LinkSourceHandler::handler)
            .map(linkUpdateHandlers::get)
            .findFirst();
    }

    private void notifyBot(Link link, String message, OffsetDateTime checkedAt) {
        LinkUpdate update = new LinkUpdate(
            link.getId(),
            URI.create(link.getUrl()),
            message,
            link.getChats().stream()
                .map(Chat::getChatId)
                .collect(Collectors.toList())
        );
        log.debug(
            "send link update to the bot: id={}\nurl={}\nmessage={}",
            update.id(),
            update.url(),
            update.description()
        );
        boolean isSent = updateSender.send(update);
        if (isSent) {
            linkService.updateCheckedAt(link, checkedAt);
        }
    }

    private void handleClientExceptionOnLinkUpdate(RuntimeException ex, Link link) {
        log.info("client error on link update: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc) {
            HttpStatusCode status = clientExc.getStatusCode();
            if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST)) {
                linkService.updateLinkStatus(link, LinkStatus.BROKEN);
            }
        }
    }
}
