package edu.java.handler.stackoverflow;

import edu.java.entity.Link;
import edu.java.service.BotService;
import edu.java.service.LinkService;
import edu.java.service.StackoverflowService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class Question implements StackoverflowSource {

    @Value("${app.source-regex.stackoverflow.question}")
    private String regex;

    private final StackoverflowService stackoverflowService;
    private final BotService botService;
    private final LinkService linkService;

    @Override
    public String urlPath() {
        return regex;
    }

    @Override
    public void checkLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        String id = matcher.group("id");
        OffsetDateTime checkedAt = OffsetDateTime.now();
        Optional<String> question = stackoverflowService.getQuestionResponse(id, link);
        Optional<String> answers = Optional.empty();
        if (question.isPresent()) {
            answers = stackoverflowService.getQuestionAnswersResponse(id, link.getCheckedAt());
        }
        String response = Stream.of(question, answers)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(StringUtils::hasText)
            .collect(Collectors.joining(":\n"));
        if (!response.isEmpty()) {
            botService.sendLinkUpdate(link, response);
        }
        linkService.updateCheckedAt(link, checkedAt);
    }
}
