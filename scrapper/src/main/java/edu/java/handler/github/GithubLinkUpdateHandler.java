package edu.java.handler.github;

import edu.java.enums.LinkType;
import edu.java.handler.LinkUpdateHandler;
import edu.java.service.LinkService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GithubLinkUpdateHandler extends LinkUpdateHandler {

    public GithubLinkUpdateHandler(List<GithubSource> sources, LinkService linkService) {
        super(LinkType.GITHUB, sources, linkService);
    }
}
