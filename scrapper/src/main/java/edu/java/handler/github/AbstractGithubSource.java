package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.handler.AbstractSource;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;

public abstract class AbstractGithubSource extends AbstractSource<GithubSource> implements GithubSource {

    private final GithubService githubService;
    private final BotService botService;
    private final LinkService linkService;
    private final LinkSourceClientExceptionHandler clientExceptionHandler;

    protected AbstractGithubSource(
        GithubService githubService,
        BotService botService,
        LinkService linkService,
        LinkSourceClientExceptionHandler clientExceptionHandler
    ) {
        this.githubService = githubService;
        this.botService = botService;
        this.linkService = linkService;
        this.clientExceptionHandler = clientExceptionHandler;
    }

    protected void processBaseIssueUpdate(Link link, boolean isIssue) {
        MatchResult matcher = linkMatcher(link);
        String owner = matcher.group("owner");
        String repo = matcher.group("repo");
        String num = matcher.group("num");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        try {
            Optional<String> response =
                isIssue ? githubService.getIssueResponse(owner, repo, num, link.getCheckedAt())
                    : githubService.getPullRequestResponse(owner, repo, num, link.getCheckedAt());
            response.ifPresent(res -> botService.sendLinkUpdate(link, res));
        } catch (RuntimeException ex) {
            clientExceptionHandler.processClientException(ex, link);
            return;
        }
        linkService.updateCheckedAt(link, checkedAt);
    }
}
