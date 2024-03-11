package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Issue extends AbstractGithubSource {

    @Value("${app.source-regex.github.issue}")
    private String regex;

    public Issue(GithubService githubService, BotService botService, LinkService linkService) {
        super(githubService, botService, linkService);
    }

    @Override
    public String urlPath() {
        return regex;
    }

    @Override
    public void checkLinkUpdate(Link link) {
        processBaseIssueUpdate(link, true);
    }
}
