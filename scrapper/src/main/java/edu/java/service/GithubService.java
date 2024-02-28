package edu.java.service;

import edu.java.client.GithubClient;
import edu.java.dto.github.CommitDto;
import edu.java.dto.github.IssueDto;
import edu.java.dto.github.RepositoryDto;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
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

    public Optional<String> getRepoCommitsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        return getCommitsResponse(repository, null, lastCheckedAt);
    }

    public Optional<String> getBranchCommitsResponse(
        RepositoryDto repository,
        String branch,
        OffsetDateTime lastCheckedAt
    ) {
        return getCommitsResponse(repository, Map.of("sha", branch), lastCheckedAt);
    }

    public Optional<String> getIssuesAndPullsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        return getIssuesAndPulls(repository, lastCheckedAt)
            .filter(issues -> !CollectionUtils.isEmpty(issues))
            .map(this::getIssuesAndPullsResponseMessage)
            .filter(StringUtils::hasText);
    }

    public Optional<String> getIssueResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        String url = getIssueUrl(repository, num);
        return getBaseIssueResponse(url, lastCheckedAt, true);
    }

    public Optional<String> getPullRequestResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        String url = getPullRequestUrl(repository, num);
        return getBaseIssueResponse(url, lastCheckedAt, false);
    }

    private Optional<String> getCommitsResponse(
        RepositoryDto repository,
        Map<String, String> params,
        OffsetDateTime lastCheckedAt
    ) {
        return getCommits(repository, params, lastCheckedAt)
            .filter(commits -> !CollectionUtils.isEmpty(commits))
            .map(this::getCommitsResponseMessage)
            .filter(StringUtils::hasText);
    }

    private Optional<List<CommitDto>> getCommits(
        RepositoryDto repository,
        Map<String, String> params,
        OffsetDateTime lastCheckedAt
    ) {
        String url = getCommitsUrl(repository);
        Map<String, String> queryParams = Stream.of(getSinceRequestParam(lastCheckedAt), params)
            .filter(Objects::nonNull)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return githubClient.doGet(url, queryParams, COMMITS_RESPONSE);
    }

    private Optional<List<IssueDto>> getIssuesAndPulls(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        String url = getCommonIssuesUrl(repository);
        Map<String, String> params = Stream.of(getSinceRequestParam(lastCheckedAt), ISSUE_STATE_PARAM)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return githubClient.doGet(url, params, ISSUES_RESPONSE);
    }

    private Optional<String> getBaseIssueResponse(String url, OffsetDateTime lastCheckedAt, boolean isIssue) {
        return githubClient.doGet(url, null, ISSUE_RESPONSE)
            .filter(issue -> issue.updatedAt().isAfter(lastCheckedAt))
            .map(issue -> getBaseIssueResponseMessage(issue, isIssue));
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

    private String getCommitsUrl(RepositoryDto repository) {
        return String.format(API_COMMITS, repository.owner(), repository.repo());
    }

    private String getCommonIssuesUrl(RepositoryDto repository) {
        return String.format(API_ISSUES, repository.owner(), repository.repo());
    }

    private String getIssueUrl(RepositoryDto repository, String num) {
        return getCommonIssuesUrl(repository) + "/" + num;
    }

    private String getPullRequestUrl(RepositoryDto repository, String num) {
        return String.format(API_PULLS, repository.owner(), repository.repo()) + "/" + num;
    }

    private Map<String, String> getSinceRequestParam(OffsetDateTime time) {
        return Map.of("since", time.format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
    }
}
