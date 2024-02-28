package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Component
public class LinkSourceClientExceptionHandler {

    private final LinkService linkService;

    public void processClientException(RuntimeException ex, Link link) {
        log.info("client error: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc) {
            HttpStatusCode status = clientExc.getStatusCode();
            if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST)) {
                linkService.updateLinkStatus(link, LinkStatus.BROKEN);
            }
        }
    }
}
