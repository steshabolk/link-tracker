package edu.java.repository;

import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {

    Link save(Link link);

    List<Link> findAllByChat(Chat chat);

    Link findByUrl(String url);

    boolean updateStatus(Link link, LinkStatus status);

    boolean updateCheckedAt(Link link, OffsetDateTime checkedAt);

    List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Integer limit);
}
