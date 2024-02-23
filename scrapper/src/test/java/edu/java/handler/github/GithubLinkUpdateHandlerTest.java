package edu.java.handler.github;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GithubLinkUpdateHandlerTest {

    private GithubLinkUpdateHandler githubLinkUpdateHandler;
    @Mock
    private Branch branch;
    @Mock
    private Issue issue;
    @Mock
    private Branch pullRequest;
    @Mock
    private Branch repository;

    @BeforeEach
    void init() {
        List<GithubSource> githubSources = List.of(branch, issue, pullRequest, repository);
        githubLinkUpdateHandler = new GithubLinkUpdateHandler(githubSources);
    }

    @Nested
    class LinkTypeTest {

        @Test
        void linkTypeTest() {
            LinkType expected = LinkType.GITHUB;

            LinkType actual = githubLinkUpdateHandler.linkType();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UpdateLinkTest {

        @Test
        void updateLinkTest() {
            OffsetDateTime checkedAt = OffsetDateTime.of(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0, 0),
                ZoneOffset.UTC
            );
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(checkedAt)
                .build();

            githubLinkUpdateHandler.updateLink(link);

            verify(branch).processLinkChain(link);
            verify(issue, never()).processLinkChain(link);
            verify(pullRequest, never()).processLinkChain(link);
            verify(repository, never()).processLinkChain(link);
        }
    }
}
