package edu.java.service;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.handler.LinkUpdateHandler;
import edu.java.handler.github.GithubLinkUpdateHandler;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LinkUpdaterServiceTest {

    private LinkUpdaterService linkUpdaterService;
    @Mock
    private GithubLinkUpdateHandler githubLinkUpdateHandler;
    @Mock
    private GithubLinkUpdateHandler stackoverflowLinkUpdateHandler;
    @Mock
    private LinkService linkService;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );

    @BeforeEach
    void init() {
        List<LinkUpdateHandler> linkUpdateHandlers = List.of(githubLinkUpdateHandler, stackoverflowLinkUpdateHandler);
        linkUpdaterService = new LinkUpdaterService(linkUpdateHandlers, linkService);
        doReturn(LinkType.GITHUB).when(githubLinkUpdateHandler).linkType();
        doReturn(LinkType.STACKOVERFLOW).when(stackoverflowLinkUpdateHandler).linkType();
    }

    @Nested
    class UpdateLinksTest {

        @Test
        void shouldTriggerGithubUpdateHandler() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(CHECKED_AT)
                .build();
            doReturn(List.of(link)).when(linkService).getActiveLinks();

            linkUpdaterService.updateLinks();

            verify(githubLinkUpdateHandler).updateLink(link);
            verify(stackoverflowLinkUpdateHandler, never()).updateLink(link);
        }

        @Test
        void shouldTriggerStackoverflowUpdateHandler() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/questions/24840667")
                .checkedAt(CHECKED_AT)
                .build();
            doReturn(List.of(link)).when(linkService).getActiveLinks();

            linkUpdaterService.updateLinks();

            verify(stackoverflowLinkUpdateHandler).updateLink(link);
            verify(githubLinkUpdateHandler, never()).updateLink(link);
        }

        @Test
        void shouldNotTriggerAnyHandlerWhenUpdateHandlerNotFound() {
            Link link = Link.builder()
                .id(1L)
                .linkType(null)
                .url("dummy")
                .checkedAt(CHECKED_AT)
                .build();
            doReturn(List.of(link)).when(linkService).getActiveLinks();

            linkUpdaterService.updateLinks();

            verify(stackoverflowLinkUpdateHandler, never()).updateLink(link);
            verify(githubLinkUpdateHandler, never()).updateLink(link);
        }
    }
}
