package edu.java.service;

import edu.java.dto.response.LinkResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.exception.ApiExceptionType;
import edu.java.util.LinkParser;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkService {

    private final Map<String, Link> links = new HashMap<>();
    private final ChatService chatService;

    public List<Link> getActiveLinks() {
        Map<Boolean, List<Link>> isTracked = new ArrayList<Link>().stream()
            .collect(Collectors.partitioningBy(
                link -> !link.getChats().isEmpty()
            ));
        isTracked.get(false).forEach(this::deleteLink);
        return isTracked.get(true);
    }

    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.getId(), status.name());
        link.setStatus(status);
    }

    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.getId(), checkedAt);
        link.setCheckedAt(checkedAt);
    }

    public LinkResponse addLink(Long chatId, URI reqLink) {
        Link parsedLink = LinkParser.parseLink(reqLink);
        Chat chat = chatService.getChat(chatId);
        Link link;
        if (links.containsKey(parsedLink.getUrl())) {
            link = links.get(parsedLink.getUrl());
        } else {
            parsedLink.setId((long) links.size());
            links.put(parsedLink.getUrl(), parsedLink);
            log.debug("new link{id={}} was saved", parsedLink.getId());
            link = parsedLink;
        }
        if (chat.getLinks().contains(link)) {
            throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(reqLink);
        }
        chat.addLink(link);
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }

    public LinkResponse removeLink(Long chatId, URI reqLink) {
        Link parsedLink = LinkParser.parseLink(reqLink);
        Chat chat = chatService.getChat(chatId);
        Link link = chat.getLinks().stream()
            .filter(it -> it.getUrl().equals(parsedLink.getUrl()))
            .findFirst()
            .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(reqLink));
        chat.removeLink(link);
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }

    private void deleteLink(Link link) {
        log.debug("link{id={}} was deleted", link.getId());
        links.remove(link.getUrl());
    }
}
