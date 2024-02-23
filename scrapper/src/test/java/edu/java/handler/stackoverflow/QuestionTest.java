package edu.java.handler.stackoverflow;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.LinkService;
import edu.java.service.StackoverflowService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionTest {

    @InjectMocks
    private Question question;
    @Mock
    private StackoverflowService stackoverflowService;
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
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.STACKOVERFLOW)
        .url("https://stackoverflow.com/questions/24840667")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class UrlPrefixTest {

        @Test
        void urlPrefixTest() {
            String expected = "https://stackoverflow.com";

            String actual = question.urlPrefix();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPatternTest {

        @Test
        void urlPatternTest() {
            String expected = "https://stackoverflow.com/questions/(?<id>[\\d]+)";

            String actual = question.urlPattern();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class LinkMatcherTest {

        @Test
        void linkMatcherTest() {
            MatchResult matcher = question.linkMatcher(LINK);

            assertThat(matcher.groupCount()).isEqualTo(1);
            assertThat(matcher.group(1)).isEqualTo("24840667");
        }
    }

    @Nested
    class CheckLinkUpdateTest {

        @Test
        void shouldSendUpdateWhenThereAreUpdates() {
            doReturn(Optional.of("question was updated"))
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));
            doReturn(Optional.of("new answers"))
                .when(stackoverflowService)
                .getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));

            question.checkLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService).getQuestionAnswersResponse("24840667", CHECKED_AT);
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService).sendLinkUpdate(LINK, "question was updated:\nnew answers");
        }

        @Test
        void shouldNotSendUpdateWhenThereAreNoUpdates() {
            doReturn(Optional.empty())
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));

            question.checkLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService, never()).getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));
            verify(linkService).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }

        @Test
        void shouldNotUpdateCheckAtWhenExceptionWasThrown() {
            doThrow(RuntimeException.class)
                .when(stackoverflowService)
                .getQuestionResponse(anyString(), any(Link.class));

            question.checkLinkUpdate(LINK);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService, never()).getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));
            verify(clientExceptionHandler).processClientException(any(RuntimeException.class), any(Link.class));
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }
    }

    @Nested
    class LinkChainTest {

        @Test
        void shouldInvokeUpdateCheckingWhenLinkMatches() {
            question.processLinkChain(LINK);

            verify(stackoverflowService).getQuestionResponse(anyString(), any(Link.class));
        }

        @Test
        void shouldInvokeNotNullNextChainElementWhenLinkDoesNotMatch() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/a/32872406")
                .checkedAt(CHECKED_AT)
                .build();

            question.processLinkChain(link);

            verify(stackoverflowService, never()).getQuestionResponse(anyString(), any(Link.class));
        }
    }
}
