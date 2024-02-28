package edu.java.handler.github;

import edu.java.dto.github.RepositoryDto;
import edu.java.entity.Link;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractGithubSource implements GithubSource {

    private final GithubService githubService;
    private final BotService botService;
    private final LinkService linkService;
    private final LinkSourceClientExceptionHandler clientExceptionHandler;

    protected void processBaseIssueUpdate(Link link, boolean isIssue) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));
        String num = matcher.group("num");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        try {
            Optional<String> response =
                isIssue ? githubService.getIssueResponse(repository, num, link.getCheckedAt())
                    : githubService.getPullRequestResponse(repository, num, link.getCheckedAt());
            response.ifPresent(res -> botService.sendLinkUpdate(link, res));
        } catch (RuntimeException ex) {
            clientExceptionHandler.processClientException(ex, link);
            return;
        }
        linkService.updateCheckedAt(link, checkedAt);
    }
}
