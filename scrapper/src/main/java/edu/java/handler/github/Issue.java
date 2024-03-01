package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import org.springframework.stereotype.Component;

@Component
public class Issue extends AbstractGithubSource {

    private static final String URL_PATH = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)";

    public Issue(GithubService githubService, BotService botService, LinkService linkService) {
        super(githubService, botService, linkService);
    }

    @Override
    public String urlPath() {
        return URL_PATH;
    }

    @Override
    public void checkLinkUpdate(Link link) {
        processBaseIssueUpdate(link, true);
    }
}
