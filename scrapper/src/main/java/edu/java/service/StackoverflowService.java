package edu.java.service;

import edu.java.client.StackoverflowClient;
import edu.java.dto.stackoverflow.QuestionAnswerDto;
import edu.java.dto.stackoverflow.QuestionDto;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
public class StackoverflowService {

    private static final String API_QUESTIONS = "/questions/%s";
    private static final String API_QUESTION_ANSWERS = API_QUESTIONS + "/answers";
    private static final String QUESTION_ANSWER_URL = "https://stackoverflow.com/a/%s";
    private static final Map<String, String> SITE_PARAM = Map.of("site", "stackoverflow");
    private static final ParameterizedTypeReference<QuestionDto> QUESTION_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final ParameterizedTypeReference<QuestionAnswerDto> QUESTION_ANSWERS_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private final StackoverflowClient stackoverflowClient;
    private final LinkService linkService;

    public Optional<String> getQuestionResponse(String id, Link link) {
        return getQuestion(id)
            .map(QuestionDto::questions)
            .flatMap(questions -> {
                if (CollectionUtils.isEmpty(questions)) {
                    linkService.updateLinkStatus(link, LinkStatus.BROKEN);
                    return Optional.empty();
                } else {
                    return Optional.of(questions.getFirst());
                }
            })
            .filter(question -> question.updatedAt().isAfter(link.getCheckedAt()))
            .map(this::getQuestionResponseMessage);
    }

    public Optional<String> getQuestionAnswersResponse(String id, OffsetDateTime lastCheckedAt) {
        return getQuestionAnswers(id)
            .map(QuestionAnswerDto::answers)
            .filter(answers -> !CollectionUtils.isEmpty(answers))
            .map(answers -> answers.stream()
                .filter(ans -> ans.updatedAt().isAfter(lastCheckedAt))
                .collect(Collectors.toList()))
            .map(this::getQuestionAnswersResponseMessage);
    }

    private Optional<QuestionDto> getQuestion(String id) {
        String url = getQuestionUrl(id);
        return stackoverflowClient.doGet(url, SITE_PARAM, QUESTION_RESPONSE);
    }

    private Optional<QuestionAnswerDto> getQuestionAnswers(String id) {
        String url = getQuestionAnswersUrl(id);
        return stackoverflowClient.doGet(url, SITE_PARAM, QUESTION_ANSWERS_RESPONSE);
    }

    private String getQuestionUrl(String id) {
        return String.format(API_QUESTIONS, id);
    }

    private String getQuestionAnswersUrl(String id) {
        return String.format(API_QUESTION_ANSWERS, id);
    }

    private String getQuestionResponseMessage(QuestionDto.Question question) {
        return String.format("◉ question [%s] was updated", question.title());
    }

    private String getQuestionAnswersResponseMessage(List<QuestionAnswerDto.Answer> answers) {
        return answers.stream()
            .map(answer -> String.format("➜ " + QUESTION_ANSWER_URL, answer.id()))
            .collect(Collectors.joining("\n"));
    }
}
