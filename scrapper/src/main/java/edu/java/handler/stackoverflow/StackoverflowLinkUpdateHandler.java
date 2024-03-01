package edu.java.handler.stackoverflow;

import edu.java.enums.LinkType;
import edu.java.handler.LinkUpdateHandler;
import edu.java.service.LinkService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StackoverflowLinkUpdateHandler extends LinkUpdateHandler {

    public StackoverflowLinkUpdateHandler(List<StackoverflowSource> sources, LinkService linkService) {
        super(LinkType.STACKOVERFLOW, sources, linkService);
    }
}
