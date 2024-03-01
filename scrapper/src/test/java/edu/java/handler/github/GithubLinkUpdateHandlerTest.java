package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.service.LinkService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GithubLinkUpdateHandlerTest {

    private GithubLinkUpdateHandler githubLinkUpdateHandler;
    @Mock
    private LinkService linkService;
    @Mock
    private RepositoryBranch repositoryBranch;
    @Mock
    private Issue issue;
    @Mock
    private PullRequest pullRequest;
    @Mock
    private Repository repository;
    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );

    @BeforeEach
    void init() {
        doReturn("https://github.com/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/tree/(?<branch>[\\w-\\./]+)")
            .when(repositoryBranch).urlPattern();
        doReturn("https://github.com/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)")
            .when(repository).urlPattern();
        doReturn("https://github.com/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/issues/(?<num>\\d+)")
            .when(issue).urlPattern();
        doReturn("https://github.com/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/pull/(?<num>\\d+)")
            .when(pullRequest).urlPattern();
        List<GithubSource> githubSources = List.of(repositoryBranch, issue, pullRequest, repository);
        githubLinkUpdateHandler = new GithubLinkUpdateHandler(githubSources, linkService);
    }

    @Nested
    class LinkTypeTest {

        @Test
        void linkTypeTest() {
            LinkType expected = LinkType.GITHUB;

            LinkType actual = githubLinkUpdateHandler.getLinkType();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UpdateLinkTest {

        @Test
        void shouldUpdateLinkWhenPatternMatches() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(CHECKED_AT)
                .build();

            githubLinkUpdateHandler.updateLink(link);

            verify(repository).checkLinkUpdate(link);
            verify(repositoryBranch, never()).checkLinkUpdate(link);
            verify(issue, never()).checkLinkUpdate(link);
            verify(pullRequest, never()).checkLinkUpdate(link);
        }

        @Test
        void shouldNotInvokeUpdateWhenPatternDoesNotMatch() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin/stargazers")
                .checkedAt(CHECKED_AT)
                .build();

            githubLinkUpdateHandler.updateLink(link);

            verify(repository, never()).checkLinkUpdate(link);
            verify(repositoryBranch, never()).checkLinkUpdate(link);
            verify(issue, never()).checkLinkUpdate(link);
            verify(pullRequest, never()).checkLinkUpdate(link);
        }

        @Test
        void shouldProcessExceptionWhen404ClientException() {
            WebClientResponseException ex = WebClientResponseException
                .create(HttpStatus.NOT_FOUND.value(), "", null, null, null);
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(CHECKED_AT)
                .build();
            doThrow(ex).when(repository).checkLinkUpdate(link);

            githubLinkUpdateHandler.updateLink(link);

            verify(linkService).updateLinkStatus(link, LinkStatus.BROKEN);
        }

        @Test
        void process400ClientExceptionTest() {
            WebClientResponseException ex = WebClientResponseException
                .create(HttpStatus.BAD_REQUEST.value(), "", null, null, null);
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(CHECKED_AT)
                .build();
            doThrow(ex).when(repository).checkLinkUpdate(link);

            githubLinkUpdateHandler.updateLink(link);

            verify(linkService).updateLinkStatus(link, LinkStatus.BROKEN);
        }
    }
}
