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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StackoverflowLinkUpdateHandlerTest {

    private StackoverflowLinkUpdateHandler stackoverflowLinkUpdateHandler;
    @Mock
    private Question question;

    @BeforeEach
    void init() {
        List<StackoverflowSource> stackoverflowSources = List.of(question);
        stackoverflowLinkUpdateHandler = new StackoverflowLinkUpdateHandler(stackoverflowSources);
    }

    @Nested
    class LinkTypeTest {

        @Test
        void linkTypeTest() {
            LinkType expected = LinkType.STACKOVERFLOW;

            LinkType actual = stackoverflowLinkUpdateHandler.linkType();

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
                .linkType(LinkType.STACKOVERFLOW)
                .url("https://stackoverflow.com/questions/24840667")
                .checkedAt(checkedAt)
                .build();

            stackoverflowLinkUpdateHandler.updateLink(link);

            verify(question).processLinkChain(link);
        }
    }
}
