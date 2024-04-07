package edu.java.handler.github;

import edu.java.dto.github.RepositoryDto;
import edu.java.entity.Link;
import edu.java.handler.LinkUpdateHandler;
import edu.java.service.GithubService;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Repository implements LinkUpdateHandler {

    @Value("${app.link-sources.github.handlers.repository.regex}")
    private String regex;
    private final GithubService githubService;

    @Override
    public String regex() {
        return regex;
    }

    @Override
    public Optional<String> getLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));
        Optional<String> commits = githubService.getRepoCommitsResponse(repository, link.getCheckedAt());
        Optional<String> issues = githubService.getIssuesAndPullsResponse(repository, link.getCheckedAt());
        return Stream.of(commits, issues)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.collectingAndThen(
                Collectors.joining("\n\n"),
                it -> it.isEmpty() ? Optional.empty() : Optional.of(it)
            ));
    }
}
