package edu.java.service;

import edu.java.client.GithubClient;
import edu.java.dto.github.CommitDto;
import edu.java.dto.github.IssueDto;
import edu.java.dto.github.RepositoryDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GithubServiceTest {

    @InjectMocks
    private GithubService githubService;
    @Mock
    private GithubClient githubClient;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final String CHECKED_AT_FORMATTED = "2024-01-01T00:00:00Z";
    private static final RepositoryDto REPOSITORY = new RepositoryDto("JetBrains", "kotlin");

    @Nested
    class RepoCommitsResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/commits";
            Map<String, String> expectedParams = Map.of("since", CHECKED_AT_FORMATTED);
            String expectedResponse = "◉ new commits were pushed:\n"
                + "➜ commit 1 [link 1]\n"
                + "➜ commit 2 [link 2]";

            List<CommitDto> commits = List.of(
                new CommitDto(new CommitDto.Commit("commit 1"), "link 1"),
                new CommitDto(new CommitDto.Commit("commit 2"), "link 2")
            );
            doReturn(Optional.of(commits)).when(githubClient).doGet(anyString(), anyMap(), any());

            Optional<String> actualResponse =
                githubService.getRepoCommitsResponse(REPOSITORY, CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/commits";
            Map<String, String> expectedParams = Map.of("since", CHECKED_AT_FORMATTED);

            List<CommitDto> commits = List.of();
            doReturn(Optional.of(commits)).when(githubClient).doGet(anyString(), anyMap(), any());

            Optional<String> actualResponse =
                githubService.getRepoCommitsResponse(REPOSITORY, CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).doGet(anyString(), anyMap(), any());

            assertThatThrownBy(() -> githubService.getRepoCommitsResponse(REPOSITORY, CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class RepositoryBranchCommitsResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/commits";
            Map<String, String> expectedParams = Map.of("since", CHECKED_AT_FORMATTED, "sha", "branch-name");
            String expectedResponse = "◉ new commits were pushed:\n"
                + "➜ commit 1 [link 1]\n"
                + "➜ commit 2 [link 2]";

            List<CommitDto> commits = List.of(
                new CommitDto(new CommitDto.Commit("commit 1"), "link 1"),
                new CommitDto(new CommitDto.Commit("commit 2"), "link 2")
            );
            doReturn(Optional.of(commits)).when(githubClient).doGet(anyString(), anyMap(), any());

            Optional<String> actualResponse =
                githubService.getBranchCommitsResponse(REPOSITORY, "branch-name", CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/commits";
            Map<String, String> expectedParams = Map.of("since", CHECKED_AT_FORMATTED, "sha", "branch-name");

            List<CommitDto> commits = List.of();
            doReturn(Optional.of(commits)).when(githubClient).doGet(anyString(), anyMap(), any());

            Optional<String> actualResponse =
                githubService.getBranchCommitsResponse(REPOSITORY, "branch-name", CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).doGet(anyString(), anyMap(), any());

            assertThatThrownBy(() -> githubService.getBranchCommitsResponse(REPOSITORY, "branch-name", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class IssuesAndPullsResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/issues";
            Map<String, String> expectedParams = Map.of("since", CHECKED_AT_FORMATTED, "state", "all");
            String expectedResponse = "◉ updates in issues and pull requests:\n"
                + "➜ issue 1 [link 1]\n"
                + "➜ issue 2 [link 2]";

            List<IssueDto> issues = List.of(
                new IssueDto("link 1", "issue 1", null),
                new IssueDto("link 2", "issue 2", null)
            );
            doReturn(Optional.of(issues)).when(githubClient).doGet(anyString(), anyMap(), any());

            Optional<String> actualResponse =
                githubService.getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/issues";
            Map<String, String> expectedParams = Map.of("since", CHECKED_AT_FORMATTED, "state", "all");

            List<IssueDto> issues = List.of();
            doReturn(Optional.of(issues)).when(githubClient).doGet(anyString(), anyMap(), any());

            Optional<String> actualResponse =
                githubService.getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).doGet(anyString(), anyMap(), any());

            assertThatThrownBy(() -> githubService.getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class IssueResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/issues/1";
            String expectedResponse = "➜ issue [title] was updated";

            IssueDto issue = new IssueDto("link", "title", CHECKED_AT.plusDays(1));
            doReturn(Optional.of(issue)).when(githubClient).doGet(anyString(), any(), any());

            Optional<String> actualResponse = githubService.getIssueResponse(REPOSITORY, "1", CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isNull();
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/issues/1";

            IssueDto issue = new IssueDto("link", "title", CHECKED_AT.minusDays(1));
            doReturn(Optional.of(issue)).when(githubClient).doGet(anyString(), any(), any());

            Optional<String> actualResponse = githubService.getIssueResponse(REPOSITORY, "1", CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isNull();
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).doGet(anyString(), any(), any());

            assertThatThrownBy(() -> githubService.getIssueResponse(REPOSITORY, "1", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class PullRequestResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/pulls/1";
            String expectedResponse = "➜ PR [title] was updated";

            IssueDto pr = new IssueDto("link", "title", CHECKED_AT.plusDays(1));
            doReturn(Optional.of(pr)).when(githubClient).doGet(anyString(), any(), any());

            Optional<String> actualResponse =
                githubService.getPullRequestResponse(REPOSITORY, "1", CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isNull();
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/repos/JetBrains/kotlin/pulls/1";

            IssueDto issue = new IssueDto("link", "title", CHECKED_AT.minusDays(1));
            doReturn(Optional.of(issue)).when(githubClient).doGet(anyString(), any(), any());

            Optional<String> actualResponse =
                githubService.getPullRequestResponse(REPOSITORY, "1", CHECKED_AT);

            verify(githubClient).doGet(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isNull();
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doThrow(new RuntimeException("client error")).when(githubClient).doGet(anyString(), any(), any());

            assertThatThrownBy(() -> githubService.getPullRequestResponse(REPOSITORY, "1", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }
}
