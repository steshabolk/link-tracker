package edu.java.handler.github;

import edu.java.dto.github.RepositoryDto;
import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.service.GithubService;
import edu.java.util.LinkSourceUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.MatchResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = PullRequest.class)
class PullRequestTest {

    @Autowired
    private PullRequest pullRequest;
    @MockBean
    private GithubService githubService;
    static MockedStatic<LinkSourceUtil> linkSourceUtilMock;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin/pull/1")
        .checkedAt(CHECKED_AT)
        .build();
    private static final RepositoryDto REPOSITORY = new RepositoryDto("JetBrains", "kotlin");

    @BeforeAll
    public static void init() {
        linkSourceUtilMock = mockStatic(LinkSourceUtil.class);
        linkSourceUtilMock.when(() -> LinkSourceUtil.getDomain(any())).thenReturn("github.com");
    }

    @AfterAll
    public static void close() {
        linkSourceUtilMock.close();
    }

    @Nested
    class RegexTest {

        @Test
        void regexTest() {
            String expected = "/(?<owner>[\\w-\\.]+)/(?<repo>[\\w-\\.]+)/pull/(?<num>\\d+)";

            String actual = pullRequest.regex();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class LinkMatcherTest {

        @Test
        void linkMatcherTest() {
            MatchResult matcher = pullRequest.linkMatcher(LINK);

            assertThat(matcher.groupCount()).isEqualTo(3);
            assertThat(matcher.group(1)).isEqualTo("JetBrains");
            assertThat(matcher.group(2)).isEqualTo("kotlin");
            assertThat(matcher.group(3)).isEqualTo("1");
        }
    }

    @Nested
    class GetLinkUpdateTest {

        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            doReturn(Optional.of("new PR"))
                .when(githubService)
                .getPullRequestResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            Optional<String> update = pullRequest.getLinkUpdate(LINK);

            verify(githubService).getPullRequestResponse(REPOSITORY, "1", CHECKED_AT);
            assertThat(update).isPresent();
            assertThat(update.get()).isEqualTo("new PR");
        }

        @Test
        void shouldReturnEmptyWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(githubService)
                .getPullRequestResponse(any(RepositoryDto.class), anyString(), any(OffsetDateTime.class));

            Optional<String> update = pullRequest.getLinkUpdate(LINK);

            verify(githubService).getPullRequestResponse(REPOSITORY, "1", CHECKED_AT);
            assertThat(update).isEmpty();
        }
    }
}
