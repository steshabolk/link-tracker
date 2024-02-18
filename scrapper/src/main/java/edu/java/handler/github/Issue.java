package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Issue extends AbstractGithubSource {

    private static final String URL_PATH = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)";
    private static String urlPattern;

    @Autowired
    public Issue(
        GithubService githubService,
        BotService botService,
        LinkService linkService,
        LinkSourceClientExceptionHandler clientExceptionHandler
    ) {
        super(githubService, botService, linkService, clientExceptionHandler);
        urlPattern = urlPrefix() + URL_PATH;
    }

    @Override
    public String urlPattern() {
        return urlPattern;
    }

    @Override
    public void checkLinkUpdate(Link link) {
        processBaseIssueUpdate(link, true);
    }
}
