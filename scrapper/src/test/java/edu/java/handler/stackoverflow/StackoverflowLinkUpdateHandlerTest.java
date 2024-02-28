package edu.java.handler.stackoverflow;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StackoverflowLinkUpdateHandlerTest {

    private StackoverflowLinkUpdateHandler stackoverflowLinkUpdateHandler;
    @Mock
    private Question question;

    @BeforeEach
    void init() {
        doReturn("https://stackoverflow.com/questions/(?<id>[\\d]+)")
            .when(question).urlPattern();
        List<StackoverflowSource> stackoverflowSources = List.of(question);
        stackoverflowLinkUpdateHandler = new StackoverflowLinkUpdateHandler(stackoverflowSources);
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
            OffsetDateTime checkedAt = OffsetDateTime.of(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0, 0),
                ZoneOffset.UTC
            );
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/questions/24840667")
                .checkedAt(checkedAt)
                .build();

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(question).checkLinkUpdate(link);
        }

        @Test
        void shouldNotInvokeUpdateWhenPatternDoesNotMatch() {
            OffsetDateTime checkedAt = OffsetDateTime.of(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0, 0),
                ZoneOffset.UTC
            );
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://stackoverflow.com/a/1")
                .checkedAt(checkedAt)
                .build();

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(question, never()).checkLinkUpdate(link);
        }
    }
}
