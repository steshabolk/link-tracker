package edu.java.handler.stackoverflow;

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
class StackoverflowLinkUpdateHandlerTest {

    private StackoverflowLinkUpdateHandler stackoverflowLinkUpdateHandler;
    @Mock
    private LinkService linkService;
    @Mock
    private Question question;
    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );

    @BeforeEach
    void init() {
        doReturn("https://stackoverflow.com/questions/(?<id>[\\d]+)")
            .when(question).urlPattern();
        List<StackoverflowSource> stackoverflowSources = List.of(question);
        stackoverflowLinkUpdateHandler = new StackoverflowLinkUpdateHandler(stackoverflowSources, linkService);
    }

    @Nested
    class LinkTypeTest {

        @Test
        void linkTypeTest() {
            LinkType expected = LinkType.STACKOVERFLOW;

            LinkType actual = stackoverflowLinkUpdateHandler.getLinkType();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UpdateLinkTest {

        @Test
        void shouldUpdateLinkWhenPatternMatches() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/questions/24840667")
                .checkedAt(CHECKED_AT)
                .build();

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(question).checkLinkUpdate(link);
        }

        @Test
        void shouldNotInvokeUpdateWhenPatternDoesNotMatch() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://stackoverflow.com/a/1")
                .checkedAt(CHECKED_AT)
                .build();

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(question, never()).checkLinkUpdate(link);
        }

        @Test
        void shouldProcessExceptionWhen404ClientException() {
            WebClientResponseException ex = WebClientResponseException
                .create(HttpStatus.NOT_FOUND.value(), "", null, null, null);
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/questions/24840667")
                .checkedAt(CHECKED_AT)
                .build();
            doThrow(ex).when(question).checkLinkUpdate(link);

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(linkService).updateLinkStatus(link, LinkStatus.BROKEN);
        }

        @Test
        void process400ClientExceptionTest() {
            WebClientResponseException ex = WebClientResponseException
                .create(HttpStatus.BAD_REQUEST.value(), "", null, null, null);
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/questions/24840667")
                .checkedAt(CHECKED_AT)
                .build();
            doThrow(ex).when(question).checkLinkUpdate(link);

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(linkService).updateLinkStatus(link, LinkStatus.BROKEN);
        }
    }
}
