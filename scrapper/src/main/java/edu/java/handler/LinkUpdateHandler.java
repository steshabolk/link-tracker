package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.service.LinkService;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class LinkUpdateHandler {

    private final LinkType linkType;
    private final List<? extends LinkSource> linkSources;
    private final LinkService linkService;

    public void updateLink(Link link) {
        linkSources.stream()
            .filter(source -> Pattern.matches(source.urlPattern(), link.getUrl()))
            .findFirst()
            .ifPresentOrElse(
                clientExceptionHandler(source -> source.checkLinkUpdate(link), link),
                () -> log.warn("link cannot be processed: {}", link.getUrl())
            );
    }

    private Consumer<LinkSource> clientExceptionHandler(Consumer<LinkSource> consumer, Link link) {
        return it -> {
            try {
                consumer.accept(it);
            } catch (RuntimeException ex) {
                log.info("client error: {}", ex.getMessage());
                if (ex instanceof WebClientResponseException clientExc) {
                    HttpStatusCode status = clientExc.getStatusCode();
                    if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST)) {
                        linkService.updateLinkStatus(link, LinkStatus.BROKEN);
                    }
                }
            }
        };
    }
}
