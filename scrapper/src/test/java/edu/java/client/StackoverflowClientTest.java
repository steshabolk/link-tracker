package edu.java.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import edu.java.dto.stackoverflow.QuestionAnswerDto;
import edu.java.dto.stackoverflow.QuestionDto;
import edu.java.exception.LinkSourceException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class StackoverflowClientTest {

    private StackoverflowClient stackoverflowClient;
    @RegisterExtension
    private static final WireMockExtension wireMockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private static final String API_QUESTION = "/questions/.*?.*";
    private static final String API_QUESTION_ANSWERS = "/questions/.*/answers?.*";
    private static final String API_400 = "/err400";
    private static final Map<String, String> SITE_PARAM = Map.of("site", "stackoverflow");
    private static final ParameterizedTypeReference<QuestionDto> QUESTION_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final ParameterizedTypeReference<QuestionAnswerDto> QUESTION_ANSWERS_RESPONSE =
        new ParameterizedTypeReference<>() {
        };

    @BeforeEach
    void init() {
        WebClient webClient = WebClient.builder().baseUrl(wireMockExtension.baseUrl()).build();
        stackoverflowClient = new StackoverflowClient(webClient);
    }

    @Nested
    class LinkUpdatesTest {

        @SneakyThrows
        @Test
        void getQuestionTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("stackoverflow/question_response.json").toURI())
            ));
            OffsetDateTime expectedUpdatedAt = OffsetDateTime.of(
                LocalDate.of(2024, 2, 20),
                LocalTime.of(8, 19, 24),
                ZoneOffset.UTC
            );
            wireMockExtension.stubFor(
                get(urlMatching(API_QUESTION))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );
            String url = "/questions/24840667";

            QuestionDto questionRes = stackoverflowClient.getLinkUpdates(url, SITE_PARAM, QUESTION_RESPONSE).block();

            assertThat(questionRes).isNotNull();
            assertThat(questionRes.questions().size()).isEqualTo(1);
            QuestionDto.Question question = questionRes.questions().get(0);
            assertThat(question.title()).isEqualTo("What is the regex to extract all the emojis from a string?");
            assertThat(question.updatedAt()).isEqualTo(expectedUpdatedAt);
        }

        @SneakyThrows
        @Test
        void getQuestionAnswersTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("stackoverflow/answers_response.json").toURI())
            ));
            List<String> expectedAnswerIds = List.of("24841069", "75528528", "77157680", "67730175");
            wireMockExtension.stubFor(
                get(urlMatching(API_QUESTION_ANSWERS))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );
            String url = "/questions/24840667/answers";

            QuestionAnswerDto answersRes =
                stackoverflowClient.getLinkUpdates(url, SITE_PARAM, QUESTION_ANSWERS_RESPONSE).block();

            assertThat(answersRes).isNotNull();
            assertThat(answersRes.answers().size()).isEqualTo(4);
            List<String> answersIds = answersRes.answers().stream()
                .map(QuestionAnswerDto.Answer::id)
                .toList();
            assertThat(answersIds).isEqualTo(expectedAnswerIds);
        }

        @SneakyThrows
        @Test
        void response400Test() {
            wireMockExtension.stubFor(
                get(urlMatching(API_400))
                    .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "error_id": 400,
                                "error_message": "ids",
                                "error_name": "bad_parameter"
                            }
                            """)
                    )
            );
            String url = "/err400";

            assertThatThrownBy(() -> stackoverflowClient.getLinkUpdates(url, null, QUESTION_RESPONSE).block())
                .isInstanceOf(LinkSourceException.class)
                .hasMessageContaining("\"error_id\": 400");
        }
    }
}
