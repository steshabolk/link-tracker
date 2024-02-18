package edu.java.handler.github;

import edu.java.enums.LinkType;
import edu.java.handler.AbstractLinkUpdateHandler;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GithubLinkUpdateHandler extends AbstractLinkUpdateHandler<GithubSource> {

    public GithubLinkUpdateHandler(List<GithubSource> sources) {
        super(LinkType.GITHUB, sources);
    }
}
