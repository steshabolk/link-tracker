package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class LinkUpdateHandler {

    private final LinkType linkType;
    private final List<? extends LinkSource> linkSources;

    public void updateLink(Link link) {
        linkSources.stream()
            .filter(source -> Pattern.matches(source.urlPattern(), link.getUrl()))
            .findFirst()
            .ifPresentOrElse(
                source -> source.checkLinkUpdate(link),
                () -> log.warn("link cannot be processed: {}", link.getUrl())
            );
    }
}
