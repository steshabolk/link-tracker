package edu.java.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import edu.java.dto.github.CommitDto;
import edu.java.dto.github.IssueDto;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class GithubClientTest {

    private static GithubClient githubClient;
    @RegisterExtension
    private static final WireMockExtension wireMockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort())
        .build();

    private static final String API_COMMITS = "/repos/.*/.*/commits.*";
    private static final String API_ISSUE = "/repos/.*/.*/issues/.*";
    private static final String API_PR = "/repos/.*/.*/pulls/.*";
    private static final String API_404 = "/err404";
    private static final Map<String, String> SINCE_PARAM = Map.of("since", "2024-01-01T00:00:00Z");
    private static final ParameterizedTypeReference<List<CommitDto>> COMMITS_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final ParameterizedTypeReference<IssueDto> ISSUE_RESPONSE =
        new ParameterizedTypeReference<>() {
        };

    @BeforeEach
    void init() {
        WebClient webClient = WebClient.builder().baseUrl(wireMockExtension.baseUrl()).build();
        githubClient = new GithubClient(webClient);
    }

    @Nested
    class GetRequestTest {

        @SneakyThrows
        @Test
        void getCommitsTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("github/commits_response.json").toURI())
            ));
            wireMockExtension.stubFor(
                get(urlMatching(API_COMMITS))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );
            String url = "/repos/golang/go/commits";

            Optional<List<CommitDto>> optionalRes = githubClient.doGet(url, SINCE_PARAM, COMMITS_RESPONSE);

            assertThat(optionalRes).isPresent();
            List<CommitDto> commits = optionalRes.get();
            assertThat(commits.size()).isEqualTo(2);
            CommitDto commit1 = commits.get(0);
            assertThat(commit1.commit().message()).startsWith("go/doc: fix typo in comment");
            assertThat(commit1.htmlUrl()).isEqualTo(
                "https://github.com/golang/go/commit/5d4e8f5162c97d9a51abbe55d0042fea9c6be3a0");
            CommitDto commit2 = commits.get(1);
            assertThat(commit2.commit().message()).startsWith("cmd: remove support for GOROOT_FINAL");
            assertThat(commit2.htmlUrl()).isEqualTo(
                "https://github.com/golang/go/commit/507d1b22f4b58ac68841582d0c2c0ab6b20e5a98");
        }

        @SneakyThrows
        @Test
        void getIssueTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("github/issue_response.json").toURI())
            ));
            OffsetDateTime expectedUpdatedAt = OffsetDateTime.of(
                LocalDate.of(2024, 2, 22),
                LocalTime.of(3, 7, 34),
                ZoneOffset.UTC
            );
            wireMockExtension.stubFor(
                get(urlMatching(API_ISSUE))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );
            String url = "/repos/golang/go/issues/65864";

            Optional<IssueDto> optionalRes = githubClient.doGet(url, null, ISSUE_RESPONSE);

            assertThat(optionalRes).isPresent();
            IssueDto issue = optionalRes.get();
            assertThat(issue.title()).isEqualTo("Potential Memory Leak in Runtime with Goroutines not being Freed");
            assertThat(issue.htmlUrl()).isEqualTo("https://github.com/golang/go/issues/65864");
            assertThat(issue.updatedAt()).isEqualTo(expectedUpdatedAt);
        }

        @SneakyThrows
        @Test
        void getPRTest() {
            String response = new String(Files.readAllBytes(
                Paths.get(ClassLoader.getSystemResource("github/pr_response.json").toURI())
            ));
            OffsetDateTime expectedUpdatedAt = OffsetDateTime.of(
                LocalDate.of(2024, 2, 22),
                LocalTime.of(0, 54, 5),
                ZoneOffset.UTC
            );
            wireMockExtension.stubFor(
                get(urlMatching(API_PR))
                    .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody(response)
                    )
            );
            String url = "/repos/golang/go/pulls/65840";

            Optional<IssueDto> optionalRes = githubClient.doGet(url, null, ISSUE_RESPONSE);

            assertThat(optionalRes).isPresent();
            IssueDto pr = optionalRes.get();
            assertThat(pr.title()).isEqualTo("cmd/compile: use quotes to wrap user-supplied token");
            assertThat(pr.htmlUrl()).isEqualTo("https://github.com/golang/go/pull/65840");
            assertThat(pr.updatedAt()).isEqualTo(expectedUpdatedAt);
        }

        @SneakyThrows
        @Test
        void response404Test() {
            wireMockExtension.stubFor(
                get(urlMatching(API_404))
                    .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                              "message": "Not Found",
                              "documentation_url": "https://docs.github.com/rest/repos/repos#get-a-repository"
                            }
                            """)
                    )
            );
            String url = "/err404";

            WebClientResponseException ex = catchThrowableOfType(
                () -> githubClient.doGet(url, null, COMMITS_RESPONSE),
                WebClientResponseException.class
            );
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
