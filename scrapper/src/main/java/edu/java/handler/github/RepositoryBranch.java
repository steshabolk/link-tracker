package edu.java.handler.github;

import edu.java.dto.github.RepositoryDto;
import edu.java.entity.Link;
import edu.java.enums.GithubRegex;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

@Component
public class RepositoryBranch extends AbstractGithubSource {

    private final GithubService githubService;
    private final BotService botService;
    private final LinkService linkService;

    public RepositoryBranch(GithubService githubService, BotService botService, LinkService linkService) {
        super(githubService, botService, linkService);
        this.githubService = githubService;
        this.botService = botService;
        this.linkService = linkService;
    }

    @Override
    public String urlPath() {
        return GithubRegex.BRANCH.regex();
    }

    @Override
    public void checkLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));
        String branch = matcher.group("branch");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        githubService.getBranchCommitsResponse(repository, branch, link.getCheckedAt())
            .ifPresent(res -> botService.sendLinkUpdate(link, res));
        linkService.updateCheckedAt(link, checkedAt);
    }
}
