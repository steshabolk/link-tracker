package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import java.util.List;
import java.util.Optional;

public abstract class AbstractLinkUpdateHandler<T extends BaseSource<T>> implements LinkUpdateHandler {

    private final LinkType linkType;
    private final BaseSource<T> chainHead;

    protected AbstractLinkUpdateHandler(LinkType linkType, List<T> sources) {
        this.linkType = linkType;
        this.chainHead = ChainElement.buildChain(sources);
    }

    @Override
    public LinkType linkType() {
        return linkType;
    }

    @Override
    public void updateLink(Link link) {
        Optional.ofNullable(chainHead)
            .ifPresent(source -> source.processLinkChain(link));
    }
}
