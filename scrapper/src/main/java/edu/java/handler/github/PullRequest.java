package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.enums.GithubRegex;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import org.springframework.stereotype.Component;

@Component
public class PullRequest extends AbstractGithubSource {

    public PullRequest(GithubService githubService, BotService botService, LinkService linkService) {
        super(githubService, botService, linkService);
    }

    @Override
    public String urlPath() {
        return GithubRegex.PULL_REQUEST.regex();
    }

    @Override
    public void checkLinkUpdate(Link link) {
        processBaseIssueUpdate(link, false);
    }
}
