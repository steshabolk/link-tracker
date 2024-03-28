package edu.java.service.jpa;

import edu.java.configuration.DatabaseAccessConfig;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.exception.ApiExceptionType;
import edu.java.repository.jpa.JpaLinkRepository;
import edu.java.service.LinkService;
import edu.java.util.LinkParser;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JpaAccessConfig.class)
@Service
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatService chatService;

    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        return linkRepository.findAllWithStatusAndOlderThan(
            LinkStatus.ACTIVE,
            OffsetDateTime.now().minusMinutes(minutes),
            PageRequest.of(0, limit)
        );
    }

    @Transactional
    @Override
    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.getId(), status.name());
        link.setStatus(status);
    }

    @Transactional
    @Override
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.getId(), checkedAt);
        link.setCheckedAt(checkedAt);
    }

    @Transactional
    @Override
    public LinkResponse addLinkToChat(Long chatId, URI url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForAdding(parsedLink, chat);
        chat.addLink(link);
        log.debug("add link{id={}} to chat{id={}}", link.getId(), chat.getId());
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }

    @Transactional
    @Override
    public LinkResponse removeLinkFromChat(Long chatId, URI url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForDeletion(parsedLink, chat);
        chat.removeLink(link);
        log.debug("remove link{id={}} from chat{id={}}", link.getId(), chat.getId());
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }

    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        List<LinkResponse> trackedLinks = chatService.findByChatId(chatId)
            .getLinks().stream()
            .map(link -> new LinkResponse(link.getId(), URI.create(link.getUrl())))
            .toList();
        return new ListLinksResponse(trackedLinks, trackedLinks.size());
    }

    private Link processLinkForAdding(Link parsedLink, Chat chat) {
        chat.findLinkByUrl(parsedLink.getUrl())
            .ifPresent(it -> {
                throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(it.getUrl());
            });
        return linkRepository
            .findByUrl(parsedLink.getUrl())
            .orElseGet(() -> linkRepository.save(parsedLink));
    }

    private Link processLinkForDeletion(Link parsedLink, Chat chat) {
        return chat.findLinkByUrl(parsedLink.getUrl())
            .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(parsedLink.getUrl()));
    }
}
