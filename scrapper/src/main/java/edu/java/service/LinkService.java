package edu.java.service;

import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkService {

    List<Link> getLinksToUpdate(Integer minutes, Integer limit);

    void updateLinkStatus(Link link, LinkStatus status);

    void updateCheckedAt(Link link, OffsetDateTime checkedAt);

    LinkResponse addLinkToChat(Long chatId, URI url);

    LinkResponse removeLinkFromChat(Long chatId, URI url);

    ListLinksResponse getChatLinks(Long chatId);
}
