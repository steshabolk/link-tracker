package edu.java.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
public class Chat {

    private Long id;

    private Long chatId;

    @Builder.Default
    private Set<Link> links = new HashSet<>();

    public void addLink(Link link) {
        links.add(link);
        link.getChats().add(this);
        log.debug("link{id={}} was added for chat{id={}}", link.getId(), id);
    }

    public void removeLink(Link link) {
        links.remove(link);
        link.getChats().remove(this);
        log.debug("link{id={}} was removed for chat{id={}}", link.getId(), id);
    }
}
