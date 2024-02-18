package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.regex.MatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Branch extends AbstractGithubSource {

    private static final String URL_PATH = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/tree/(?<branch>[\\w-\\./]+)";
    private static String urlPattern;
    private final GithubService githubService;
    private final BotService botService;
    private final LinkService linkService;
    private final LinkSourceClientExceptionHandler clientExceptionHandler;

    @Autowired
    public Branch(
        GithubService githubService,
        BotService botService,
        LinkService linkService,
        LinkSourceClientExceptionHandler clientExceptionHandler
    ) {
        super(githubService, botService, linkService, clientExceptionHandler);
        urlPattern = urlPrefix() + URL_PATH;
        this.githubService = githubService;
        this.botService = botService;
        this.linkService = linkService;
        this.clientExceptionHandler = clientExceptionHandler;
    }

    @Override
    public String urlPattern() {
        return urlPattern;
    }

    @Override
    public void checkLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        String owner = matcher.group("owner");
        String repo = matcher.group("repo");
        String branch = matcher.group("branch");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        try {
            githubService.getBranchCommitsResponse(owner, repo, branch, link.getCheckedAt())
                .ifPresent(res -> botService.sendLinkUpdate(link, res));
        } catch (RuntimeException ex) {
            clientExceptionHandler.processClientException(ex, link);
            return;
        }
        linkService.updateCheckedAt(link, checkedAt);
    }
}
