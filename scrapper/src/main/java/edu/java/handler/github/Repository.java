package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Repository extends AbstractGithubSource {

    private static final String URL_PATH = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)";
    private static String urlPattern;
    private final GithubService githubService;
    private final BotService botService;
    private final LinkService linkService;
    private final LinkSourceClientExceptionHandler clientExceptionHandler;

    @Autowired
    public Repository(
        GithubService githubService, BotService botService,
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
        OffsetDateTime checkedAt = OffsetDateTime.now();
        String response;
        try {
            Optional<String> commits = githubService.getRepoCommitsResponse(owner, repo, link.getCheckedAt());
            Optional<String> issues = githubService.getIssuesAndPullsResponse(owner, repo, link.getCheckedAt());
            response = Stream.of(commits, issues)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("\n\n"));
        } catch (RuntimeException ex) {
            clientExceptionHandler.processClientException(ex, link);
            return;
        }
        if (!response.isEmpty()) {
            botService.sendLinkUpdate(link, response);
        }
        linkService.updateCheckedAt(link, checkedAt);
    }
}
