package edu.java.scheduler;

import edu.java.service.LinkUpdaterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = {"app.scheduler.enable"}, havingValue = "true")
public class LinkUpdaterScheduler {

    private final LinkUpdaterService linkUpdaterService;

    @Autowired
    public LinkUpdaterScheduler(LinkUpdaterService linkUpdaterService) {
        this.linkUpdaterService = linkUpdaterService;
    }

    @Scheduled(initialDelayString = "#{linkUpdateForceDelay}", fixedDelayString = "#{linkUpdateInterval}")
    public void update() {
        log.debug("the link update task has been started");
        linkUpdaterService.updateLinks();
    }
}
