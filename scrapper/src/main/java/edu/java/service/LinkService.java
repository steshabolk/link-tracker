package edu.java.service;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkService {

    public List<Link> getActiveLinks() {
        return List.of();
    }

    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.getId(), status.name());
        link.setStatus(status);
    }

    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.getId(), checkedAt);
        link.setCheckedAt(checkedAt);
    }
}
