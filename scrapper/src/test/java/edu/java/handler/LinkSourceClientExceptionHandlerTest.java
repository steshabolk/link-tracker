package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.exception.LinkSourceError;
import edu.java.exception.LinkSourceException;
import edu.java.service.LinkService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LinkSourceClientExceptionHandlerTest {

    @InjectMocks
    private LinkSourceClientExceptionHandler clientExceptionHandler;
    @Mock
    private LinkService linkService;

    @Nested
    class ProcessClientException {

        @Test
        void processClientExceptionTest() {
            OffsetDateTime checkedAt = OffsetDateTime.of(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0, 0),
                ZoneOffset.UTC
            );
            LinkSourceException ex = LinkSourceError.BROKEN_LINK.toException("");
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(checkedAt)
                .build();

            clientExceptionHandler.processClientException(ex, link);

            verify(linkService).updateLinkStatus(link, LinkStatus.BROKEN);
        }
    }

}
