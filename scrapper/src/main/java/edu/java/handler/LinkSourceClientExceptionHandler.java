package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.exception.LinkSourceError;
import edu.java.exception.LinkSourceException;
import edu.java.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkSourceClientExceptionHandler {

    private final LinkService linkService;

    @Autowired
    public LinkSourceClientExceptionHandler(LinkService linkService) {
        this.linkService = linkService;
    }

    public void processClientException(RuntimeException ex, Link link) {
        log.info("client error: {}", ex.getMessage());
        if (ex instanceof LinkSourceException linkExc) {
            if (linkExc.getCode().equals(LinkSourceError.BROKEN_LINK.name())) {
                linkService.updateLinkStatus(link, LinkStatus.BROKEN);
            }
        }
    }
}
