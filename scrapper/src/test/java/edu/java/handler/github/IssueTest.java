package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.handler.LinkSourceClientExceptionHandler;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
    @Mock
    private LinkSourceClientExceptionHandler clientExceptionHandler;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin/issues/1")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class UrlPrefixTest {

        @Test
        void urlPrefixTest() {
            String expected = "https://github.com";

            String actual = issue.urlPrefix();

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
                .getIssueResponse(anyString(), anyString(), anyString(), any(OffsetDateTime.class));

            issue.checkLinkUpdate(LINK);

            verify(githubService).getIssueResponse("JetBrains", "kotlin", "1", CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService).sendLinkUpdate(LINK, "new issues");
        }

        @Test
        void shouldNotSendUpdateWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(githubService)
                .getIssueResponse(anyString(), anyString(), anyString(), any(OffsetDateTime.class));

            issue.checkLinkUpdate(LINK);

            verify(githubService).getIssueResponse("JetBrains", "kotlin", "1", CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }

        @Test
        void shouldNotUpdateCheckAtWhenExceptionWasThrown() {
            doThrow(RuntimeException.class)
                .when(githubService)
                .getIssueResponse(anyString(), anyString(), anyString(), any(OffsetDateTime.class));

            issue.checkLinkUpdate(LINK);

            verify(githubService).getIssueResponse("JetBrains", "kotlin", "1", CHECKED_AT);
            verify(clientExceptionHandler).processClientException(any(RuntimeException.class), any(Link.class));
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }
    }

    @Nested
    class LinkChainTest {

        @Test
        void shouldInvokeUpdateCheckingWhenLinkMatches() {
            issue.processLinkChain(LINK);

            verify(githubService).getIssueResponse(anyString(), anyString(), anyString(), any(OffsetDateTime.class));
        }

        @Test
        void shouldInvokeNotNullNextChainElementWhenLinkDoesNotMatch() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(CHECKED_AT)
                .build();
            Repository repo = mock(Repository.class);
            issue.setNext(repo);

            issue.processLinkChain(link);

            verify(githubService, never()).getIssueResponse(
                anyString(),
                anyString(),
                anyString(),
                any(OffsetDateTime.class)
            );
            verify(repo).processLinkChain(link);
        }
    }
}
