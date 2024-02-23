package edu.java.service;

import edu.java.client.StackoverflowClient;
import edu.java.dto.stackoverflow.QuestionAnswerDto;
import edu.java.dto.stackoverflow.QuestionDto;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StackoverflowServiceTest {

    @InjectMocks
    private StackoverflowService stackoverflowService;
    @Mock
    private StackoverflowClient stackoverflowClient;
    @Mock
    private LinkService linkService;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK = Link.builder()
        .id(1L)
        .linkType(LinkType.STACKOVERFLOW)
        .url("https://stackoverflow.com/questions/24840667")
        .checkedAt(CHECKED_AT)
        .build();

    @Nested
    class QuestionResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/questions/24840667";
            Map<String, String> expectedParams = Map.of("site", "stackoverflow");
            String expectedResponse = "◉ question [title] was updated";

            QuestionDto question = new QuestionDto(List.of(new QuestionDto.Question(CHECKED_AT.plusDays(1), "title")));
            doReturn(Mono.just(question)).when(stackoverflowClient).getLinkUpdates(anyString(), anyMap(), any());

            Optional<String> actualResponse = stackoverflowService.getQuestionResponse("24840667", LINK);

            verify(stackoverflowClient).getLinkUpdates(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/questions/24840667";
            Map<String, String> expectedParams = Map.of("site", "stackoverflow");

            QuestionDto question = new QuestionDto(List.of(new QuestionDto.Question(CHECKED_AT.minusDays(1), "title")));
            doReturn(Mono.just(question)).when(stackoverflowClient).getLinkUpdates(anyString(), anyMap(), any());

            Optional<String> actualResponse = stackoverflowService.getQuestionResponse("24840667", LINK);

            verify(stackoverflowClient).getLinkUpdates(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isEmpty();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenClientResponseIsEmpty() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/questions/24840667";
            Map<String, String> expectedParams = Map.of("site", "stackoverflow");

            QuestionDto question = new QuestionDto(List.of());
            doReturn(Mono.just(question)).when(stackoverflowClient).getLinkUpdates(anyString(), anyMap(), any());

            Optional<String> actualResponse = stackoverflowService.getQuestionResponse("24840667", LINK);

            verify(stackoverflowClient).getLinkUpdates(urlCaptor.capture(), paramsCaptor.capture(), any());
            verify(linkService).updateLinkStatus(LINK, LinkStatus.BROKEN);
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doReturn(Mono.error(new RuntimeException("client error"))).when(stackoverflowClient)
                .getLinkUpdates(anyString(), anyMap(), any());

            assertThatThrownBy(() -> stackoverflowService.getQuestionResponse("24840667", LINK))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }

    @Nested
    class QuestionAnswersResponseTest {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnResponseWhenThereAreUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/questions/24840667/answers";
            Map<String, String> expectedParams = Map.of("site", "stackoverflow");
            String expectedResponse = "➜ https://stackoverflow.com/a/1";

            QuestionAnswerDto answer = new QuestionAnswerDto(List.of(
                new QuestionAnswerDto.Answer(CHECKED_AT.plusDays(1), "1", "24840667"),
                new QuestionAnswerDto.Answer(CHECKED_AT.minusDays(1), "2", "24840667")
            ));
            doReturn(Mono.just(answer)).when(stackoverflowClient).getLinkUpdates(anyString(), anyMap(), any());

            Optional<String> actualResponse = stackoverflowService.getQuestionAnswersResponse("24840667", CHECKED_AT);

            verify(stackoverflowClient).getLinkUpdates(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isPresent();
            assertThat(actualResponse.get()).isEqualTo(expectedResponse);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyResponseWhenThereAreNoUpdates() {
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);

            String expectedUrl = "/questions/24840667/answers";
            Map<String, String> expectedParams = Map.of("site", "stackoverflow");

            doReturn(Mono.just(new QuestionAnswerDto(List.of()))).when(stackoverflowClient)
                .getLinkUpdates(anyString(), anyMap(), any());

            Optional<String> actualResponse = stackoverflowService.getQuestionAnswersResponse("24840667", CHECKED_AT);

            verify(stackoverflowClient).getLinkUpdates(urlCaptor.capture(), paramsCaptor.capture(), any());
            assertThat(urlCaptor.getValue()).isEqualTo(expectedUrl);
            assertThat(paramsCaptor.getValue()).isEqualTo(expectedParams);
            assertThat(actualResponse).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenClientTrowException() {
            doReturn(Mono.error(new RuntimeException("client error"))).when(stackoverflowClient)
                .getLinkUpdates(anyString(), anyMap(), any());

            assertThatThrownBy(() -> stackoverflowService.getQuestionAnswersResponse("24840667", CHECKED_AT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("client error");
        }
    }
}
