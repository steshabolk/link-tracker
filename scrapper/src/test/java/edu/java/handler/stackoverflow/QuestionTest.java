package edu.java.handler.stackoverflow;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
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

@SpringBootTest(classes = Question.class)
class QuestionTest {

    @Autowired
    private Question question;
    @MockBean
    private StackoverflowService stackoverflowService;
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
        .linkType(LinkType.STACKOVERFLOW)
        .url("https://stackoverflow.com/questions/24840667")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class UrlDomainTest {

        @Test
        void urlDomainTest() {
            String expected = "stackoverflow.com";

            String actual = question.urlDomain();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPathTest {

        @Test
        void urlPathTest() {
            String expected = "/(?:questions|q)/(?<id>[\\d]+)[/\\w-\\d]*";

            String actual = question.urlPath();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class UrlPatternTest {

        @Test
        void urlPatternTest() {
            String expected = "https://stackoverflow.com/(?:questions|q)/(?<id>[\\d]+)[/\\w-\\d]*";

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

            assertThatThrownBy(() -> question.checkLinkUpdate(LINK)).isInstanceOf(RuntimeException.class);

            verify(stackoverflowService).getQuestionResponse("24840667", LINK);
            verify(stackoverflowService, never()).getQuestionAnswersResponse(anyString(), any(OffsetDateTime.class));
            verify(linkService, never()).updateCheckedAt(any(Link.class), any(OffsetDateTime.class));
            verify(botService, never()).sendLinkUpdate(any(Link.class), anyString());
        }
    }
}
