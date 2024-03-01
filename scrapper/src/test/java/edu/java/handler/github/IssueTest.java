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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueTest {

    @InjectMocks
    private Issue issue;
    @Mock
    private GithubService githubService;
    @Mock
    private BotService botService;
    @Mock
    private LinkService linkService;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin/issues/1")
        .checkedAt(CHECKED_AT)
        .build();
    private static final RepositoryDto REPOSITORY = new RepositoryDto("JetBrains", "kotlin");

    @Nested
    class UrlDomainTest {

        @Test
        void urlDomainTest() {
            String expected = "github.com";

            String actual = issue.urlDomain();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPathTest {

        @Test
        void urlPathTest() {
            String expected = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)";

            String actual = issue.urlPath();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPatternTest {

        @Test
        void urlPatternTest() {
            String expected = "https://github.com/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)";

            String actual = issue.urlPattern();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class LinkMatcherTest {

        @Test
        void linkMatcherTest() {
            MatchResult matcher = issue.linkMatcher(LINK);

            assertThat(matcher.groupCount()).isEqualTo(3);
            assertThat(matcher.group(1)).isEqualTo("JetBrains");
            assertThat(matcher.group(2)).isEqualTo("kotlin");
            assertThat(matcher.group(3)).isEqualTo("1");
        }
    }

    @Nested
    class CheckLinkUpdateTest {

        @Test
        void shouldSendUpdateWhenThereAreUpdates() {
            doReturn(Optional.of("new issues"))
                .when(githubService)
                .getIssueResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            issue.checkLinkUpdate(LINK);

            verify(githubService).getIssueResponse(REPOSITORY, "1", CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService).sendLinkUpdate(LINK, "new issues");
        }

        @Test
        void shouldNotSendUpdateWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(githubService)
                .getIssueResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            issue.checkLinkUpdate(LINK);

            verify(githubService).getIssueResponse(REPOSITORY, "1", CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }

        @Test
        void shouldNotUpdateCheckAtWhenExceptionWasThrown() {
            doThrow(RuntimeException.class)
                .when(githubService)
                .getIssueResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            assertThatThrownBy(() -> issue.checkLinkUpdate(LINK)).isInstanceOf(RuntimeException.class);

            verify(githubService).getIssueResponse(REPOSITORY, "1", CHECKED_AT);
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }
    }
}
