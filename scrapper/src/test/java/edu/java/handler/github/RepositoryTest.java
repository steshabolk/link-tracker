package edu.java.handler.github;

import edu.java.dto.github.RepositoryDto;
import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.service.BotService;
import edu.java.service.GithubService;
import edu.java.service.LinkService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.MatchResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = Repository.class)
class RepositoryTest {

    @Autowired
    private Repository repository;
    @MockBean
    private GithubService githubService;
    @MockBean
    private BotService botService;
    @MockBean
    private LinkService linkService;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin")
        .checkedAt(CHECKED_AT)
        .build();
    private static final RepositoryDto REPOSITORY = new RepositoryDto("JetBrains", "kotlin");

    @Nested
    class UrlDomainTest {

        @Test
        void urlDomainTest() {
            String expected = "github.com";

            String actual = repository.urlDomain();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPathTest {

        @Test
        void urlPathTest() {
            String expected = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)";

            String actual = repository.urlPath();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPatternTest {

        @Test
        void urlPatternTest() {
            String expected = "https://github.com/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)";

            String actual = repository.urlPattern();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class LinkMatcherTest {

        @Test
        void linkMatcherTest() {
            MatchResult matcher = repository.linkMatcher(LINK);

            assertThat(matcher.groupCount()).isEqualTo(2);
            assertThat(matcher.group(1)).isEqualTo("JetBrains");
            assertThat(matcher.group(2)).isEqualTo("kotlin");
        }
    }

    @Nested
    class CheckLinkUpdateTest {

        @Test
        void shouldSendUpdateWhenThereAreUpdates() {
            doReturn(Optional.of("new commits"))
                .when(githubService)
                .getRepoCommitsResponse(any(RepositoryDto.class), any(OffsetDateTime.class));
            doReturn(Optional.of("new issues"))
                .when(githubService)
                .getIssuesAndPullsResponse(any(RepositoryDto.class), any(OffsetDateTime.class));

            repository.checkLinkUpdate(LINK);

            verify(githubService).getRepoCommitsResponse(REPOSITORY, CHECKED_AT);
            verify(githubService).getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService).sendLinkUpdate(LINK, "new commits\n\nnew issues");
        }

        @Test
        void shouldNotSendUpdateWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(githubService)
                .getRepoCommitsResponse(any(RepositoryDto.class), any(OffsetDateTime.class));
            doReturn(Optional.empty())
                .when(githubService)
                .getIssuesAndPullsResponse(any(RepositoryDto.class), any(OffsetDateTime.class));

            repository.checkLinkUpdate(LINK);

            verify(githubService).getRepoCommitsResponse(REPOSITORY, CHECKED_AT);
            verify(githubService).getIssuesAndPullsResponse(REPOSITORY, CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }

        @Test
        void shouldNotUpdateCheckAtWhenExceptionWasThrown() {
            doThrow(RuntimeException.class)
                .when(githubService)
                .getRepoCommitsResponse(any(RepositoryDto.class), any(OffsetDateTime.class));

            assertThatThrownBy(() -> repository.checkLinkUpdate(LINK)).isInstanceOf(RuntimeException.class);

            verify(githubService).getRepoCommitsResponse(REPOSITORY, CHECKED_AT);
            verify(githubService, never()).getIssuesAndPullsResponse(
                any(RepositoryDto.class),
                any(OffsetDateTime.class)
            );
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }
    }
}
