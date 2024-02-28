package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LinkSourceClientExceptionHandlerTest {

    @InjectMocks
    private LinkSourceClientExceptionHandler clientExceptionHandler;
    @Mock
    private LinkService linkService;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.GITHUB)
        .url("https://github.com/JetBrains/kotlin")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class ProcessClientException {

        @Test
        void process404ClientExceptionTest() {
            WebClientResponseException ex = WebClientResponseException
                .create(HttpStatus.NOT_FOUND.value(), "", null, null, null);

            clientExceptionHandler.processClientException(ex, LINK);

            verify(linkService).updateLinkStatus(LINK, LinkStatus.BROKEN);
        }

        @Test
        void process400ClientExceptionTest() {
            WebClientResponseException ex = WebClientResponseException
                .create(HttpStatus.BAD_REQUEST.value(), "", null, null, null);

            clientExceptionHandler.processClientException(ex, LINK);

            verify(linkService).updateLinkStatus(LINK, LinkStatus.BROKEN);
        }
    }

}
