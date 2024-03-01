package edu.java.handler.github;

import edu.java.dto.github.RepositoryDto;
import edu.java.entity.Link;
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

    protected void processBaseIssueUpdate(Link link, boolean isIssue) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));
        String num = matcher.group("num");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        Optional<String> response =
            isIssue ? githubService.getIssueResponse(repository, num, link.getCheckedAt())
                : githubService.getPullRequestResponse(repository, num, link.getCheckedAt());
        response.ifPresent(res -> botService.sendLinkUpdate(link, res));
        linkService.updateCheckedAt(link, checkedAt);
    }
}
