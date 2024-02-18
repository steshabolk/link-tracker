package edu.java.handler.stackoverflow;

import edu.java.entity.Link;
import edu.java.handler.LinkSourceClientExceptionHandler;
import edu.java.service.BotService;
import edu.java.service.LinkService;
import edu.java.service.StackoverflowService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Question extends AbstractStackoverflowSource {

    private static final String URL_PATH = "/questions/(?<id>[\\d]+)";
    private static String urlPattern;
    private final StackoverflowService stackoverflowService;
    private final BotService botService;
    private final LinkService linkService;
    private final LinkSourceClientExceptionHandler clientExceptionHandler;

    @Autowired
    public Question(
        StackoverflowService stackoverflowService,
        BotService botService,
        LinkService linkService,
        LinkSourceClientExceptionHandler clientExceptionHandler
    ) {
        this.stackoverflowService = stackoverflowService;
        this.botService = botService;
        this.linkService = linkService;
        this.clientExceptionHandler = clientExceptionHandler;
        urlPattern = urlPrefix() + URL_PATH;
    }

    @Override
    public String urlPattern() {
        return urlPattern;
    }

    @Override
    public void checkLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        String id = matcher.group("id");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        String response;
        try {
            Optional<String> question = stackoverflowService.getQuestionResponse(id, link);
            Optional<String> answers = Optional.empty();
            if (question.isPresent()) {
                answers = stackoverflowService.getQuestionAnswersResponse(id, link.getCheckedAt());
            }
            response = Stream.of(question, answers)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(":\n"));
        } catch (RuntimeException ex) {
            clientExceptionHandler.processClientException(ex, link);
            return;
        }
        if (!response.isEmpty()) {
            botService.sendLinkUpdate(link, response);
        }
        linkService.updateCheckedAt(link, checkedAt);
    }
}
