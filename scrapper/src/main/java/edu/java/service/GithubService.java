package edu.java.service;

import edu.java.client.GithubClient;
import edu.java.dto.github.CommitDto;
import edu.java.dto.github.IssueDto;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
public class GithubService {

    private static final String API_COMMITS = "/repos/%s/%s/commits";
    private static final String API_ISSUES = "/repos/%s/%s/issues";
    private static final String API_PULLS = "/repos/%s/%s/pulls";
    private static final ParameterizedTypeReference<List<CommitDto>> COMMITS_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final ParameterizedTypeReference<List<IssueDto>> ISSUES_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final ParameterizedTypeReference<IssueDto> ISSUE_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final Map<String, String> ISSUE_STATE_PARAM = Map.of("state", "all");
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final GithubClient githubClient;

    @Autowired
    public GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    public Optional<String> getRepoCommitsResponse(String owner, String repo, OffsetDateTime lastCheckedAt) {
        return getCommitsResponse(owner, repo, null, lastCheckedAt);
    }

    public Optional<String> getBranchCommitsResponse(
        String owner,
        String repo,
        String branch,
        OffsetDateTime lastCheckedAt
    ) {
        return getCommitsResponse(owner, repo, Map.of("sha", branch), lastCheckedAt);
    }

    public Optional<String> getIssuesAndPullsResponse(String owner, String repo, OffsetDateTime lastCheckedAt) {
        return getIssuesAndPulls(owner, repo, lastCheckedAt)
            .filter(issues -> !CollectionUtils.isEmpty(issues))
            .map(this::getIssuesAndPullsResponseMessage)
            .filter(StringUtils::hasText)
            .blockOptional();
    }

    public Optional<String> getIssueResponse(String owner, String repo, String num, OffsetDateTime lastCheckedAt) {
        String url = getIssueUrl(owner, repo, num);
        return getBaseIssueResponse(url, lastCheckedAt, true);
    }

    public Optional<String> getPullRequestResponse(
        String owner,
        String repo,
        String num,
        OffsetDateTime lastCheckedAt
    ) {
        String url = getPullRequestUrl(owner, repo, num);
        return getBaseIssueResponse(url, lastCheckedAt, false);
    }

    private Optional<String> getCommitsResponse(
        String owner,
        String repo,
        Map<String, String> params,
        OffsetDateTime lastCheckedAt
    ) {
        return getCommits(owner, repo, params, lastCheckedAt)
            .filter(commits -> !CollectionUtils.isEmpty(commits))
            .map(this::getCommitsResponseMessage)
            .filter(StringUtils::hasText)
            .blockOptional();
    }

    private Mono<List<CommitDto>> getCommits(
        String owner,
        String repo,
        Map<String, String> params,
        OffsetDateTime lastCheckedAt
    ) {
        String url = getCommitsUrl(owner, repo);
        Map<String, String> queryParams = Stream.of(getSinceRequestParam(lastCheckedAt), params)
            .filter(Objects::nonNull)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return githubClient.getLinkUpdates(url, queryParams, COMMITS_RESPONSE);
    }

    private Mono<List<IssueDto>> getIssuesAndPulls(String owner, String repo, OffsetDateTime lastCheckedAt) {
        String url = getCommonIssuesUrl(owner, repo);
        Map<String, String> params = Stream.of(getSinceRequestParam(lastCheckedAt), ISSUE_STATE_PARAM)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return githubClient.getLinkUpdates(url, params, ISSUES_RESPONSE);
    }

    private Optional<String> getBaseIssueResponse(String url, OffsetDateTime lastCheckedAt, boolean isIssue) {
        return githubClient.getLinkUpdates(url, null, ISSUE_RESPONSE)
            .filter(issue -> issue.updatedAt().isAfter(lastCheckedAt))
            .map(issue -> getBaseIssueResponseMessage(issue, isIssue))
            .blockOptional();
    }

    private String getCommitsResponseMessage(List<CommitDto> commits) {
        return "◉ new commits were pushed:\n"
            + commits.stream()
            .map(CommitDto::getResponseBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    private String getIssuesAndPullsResponseMessage(List<IssueDto> issues) {
        return "◉ updates in issues and pull requests:\n"
            + issues.stream()
            .map(IssueDto::getResponseBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    private String getBaseIssueResponseMessage(IssueDto issue, boolean isIssue) {
        return String.format("➜ %s [%s] was updated", isIssue ? "issue" : "PR", issue.title());
    }

    private String getCommitsUrl(String owner, String repo) {
        return String.format(API_COMMITS, owner, repo);
    }

    private String getCommonIssuesUrl(String owner, String repo) {
        return String.format(API_ISSUES, owner, repo);
    }

    private String getIssueUrl(String owner, String repo, String num) {
        return getCommonIssuesUrl(owner, repo) + "/" + num;
    }

    private String getPullRequestUrl(String owner, String repo, String num) {
        return String.format(API_PULLS, owner, repo) + "/" + num;
    }

    private Map<String, String> getSinceRequestParam(OffsetDateTime time) {
        return Map.of("since", time.format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
    }
}
