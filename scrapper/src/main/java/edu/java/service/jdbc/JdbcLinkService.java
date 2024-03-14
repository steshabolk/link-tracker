package edu.java.service.jdbc;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.exception.ApiExceptionType;
import edu.java.repository.jdbc.JdbcChatLinkRepository;
import edu.java.repository.jdbc.JdbcLinkRepository;
import edu.java.service.LinkService;
import edu.java.util.LinkParser;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;
    private final JdbcChatLinkRepository chatLinkRepository;
    private final JdbcChatService chatService;

    @Transactional(readOnly = true)
    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        return linkRepository.findAllWithStatusAndOlderThan(
            LinkStatus.ACTIVE,
            OffsetDateTime.now().minusMinutes(minutes),
            limit
        );
    }

    @Transactional
    @Override
    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.getId(), status.name());
        linkRepository.updateStatus(link, status);
    }

    @Transactional
    @Override
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.getId(), checkedAt);
        linkRepository.updateCheckedAt(link, checkedAt);
    }

    @Transactional
    @Override
    public LinkResponse addLinkToChat(Long chatId, URI url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForAdding(parsedLink, chat);
        chatLinkRepository.addLinkToChat(chat, link);
        log.debug("add link{id={}} to chat{id={}}", link.getId(), chat.getId());
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }

    @Transactional
    @Override
    public LinkResponse removeLinkFromChat(Long chatId, URI url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatService.findByChatId(chatId);
        Link link = processLinkForDeletion(parsedLink, chat);
        chatLinkRepository.removeLinkFromChat(chat, link);
        log.debug("remove link{id={}} from chat{id={}}", link.getId(), chat.getId());
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }

    @Transactional(readOnly = true)
    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        Chat chat = chatService.findByChatId(chatId);
        List<LinkResponse> trackedLinks =
            linkRepository.findAllByChat(chat).stream()
                .map(link -> new LinkResponse(link.getId(), URI.create(link.getUrl())))
                .toList();
        return new ListLinksResponse(trackedLinks, trackedLinks.size());
    }

    private Link processLinkForAdding(Link parsedLink, Chat chat) {
        return Optional.ofNullable(linkRepository.findByUrl(parsedLink.getUrl()))
            .filter(it -> canLinkBeAdded(it, chat))
            .orElseGet(() -> {
                Link saved = linkRepository.save(parsedLink);
                log.debug("new link{id={}} was saved", saved.getId());
                return saved;
            });
    }

    private Link processLinkForDeletion(Link parsedLink, Chat chat) {
        return Optional.ofNullable(linkRepository.findByUrl(parsedLink.getUrl()))
            .filter(it -> chatLinkRepository.isLinkAddedToChat(chat, it))
            .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(parsedLink.getUrl()));
    }

    private boolean canLinkBeAdded(Link link, Chat chat) {
        if (chatLinkRepository.isLinkAddedToChat(chat, link)) {
            throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(link.getUrl());
        }
        return true;
    }
}
