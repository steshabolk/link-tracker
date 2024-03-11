package edu.java.util;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.exception.ApiException;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class LinkParserTest {

    static MockedStatic<LinkTypeUtil> linkTypeUtilMock;

    @BeforeAll
    public static void init() {
        linkTypeUtilMock = mockStatic(LinkTypeUtil.class);
    }

    @AfterAll
    public static void close() {
        linkTypeUtilMock.close();
    }

    @Nested
    class ParseLinkTest {

        @ParameterizedTest
        @MethodSource("edu.java.util.LinkParserTest#validLink")
        void shouldReturnParsedLinkWhenLinkIsValid(String url, LinkType linkType) {
            linkTypeUtilMock.when(() -> LinkTypeUtil.getLinkType(anyString(), anyString()))
                .thenReturn(Optional.of(linkType));

            Link actual = LinkParser.parseLink(URI.create(url));

            assertThat(actual.getUrl()).isEqualTo(url);
            assertThat(actual.getLinkType()).isEqualTo(linkType);
        }

        @ParameterizedTest
        @MethodSource("edu.java.util.LinkParserTest#invalidLink")
        void shouldThrowExceptionWhenLinkIsInvalid(String url) {
            ApiException ex = catchThrowableOfType(
                () -> LinkParser.parseLink(URI.create(url)),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("INVALID_LINK");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.invalidLink");
            assertThat(ex.getMessageArgs().length).isEqualTo(0);
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ParameterizedTest
        @MethodSource("edu.java.util.LinkParserTest#unsupportedLink")
        void shouldThrowExceptionWhenLinkIsNotSupported(String url) {
            linkTypeUtilMock.when(() -> LinkTypeUtil.getLinkType(anyString(), anyString()))
                .thenReturn(java.util.Optional.empty());

            ApiException ex = catchThrowableOfType(
                () -> LinkParser.parseLink(URI.create(url)),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("NOT_SUPPORTED_SOURCE");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.notSupportedSource");
            assertThat(ex.getMessageArgs().length).isEqualTo(0);
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    static Stream<Arguments> validLink() {
        return Stream.of(
            Arguments.of("https://github.com/JetBrains/kotlin", LinkType.GITHUB),
            Arguments.of("https://github.com/JetBrains/kotlin/tree/branch-name", LinkType.GITHUB),
            Arguments.of("https://github.com/JetBrains/kotlin/pull/1", LinkType.GITHUB),
            Arguments.of("https://github.com/JetBrains/kotlin/issues/1", LinkType.GITHUB),
            Arguments.of("https://stackoverflow.com/questions/24840667", LinkType.STACKOVERFLOW)
        );
    }

    static Stream<Arguments> invalidLink() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("dummy"),
            Arguments.of("github.com/JetBrains/kotlin"),
            Arguments.of("http:/github.com/JetBrains/kotlin"),
            Arguments.of("https://stackoverflow.com/search!q=exception")
        );
    }

    static Stream<Arguments> unsupportedLink() {
        return Stream.of(
            Arguments.of("https://www.baeldung.com/mockito-series"),
            Arguments.of("https://leetcode.com/problemset/algorithms/"),
            Arguments.of("https://github.com"),
            Arguments.of("https://stackoverflow.com"),
            Arguments.of("https://github.com/JetBrains/kotlin/releases"),
            Arguments.of("https://github.com/JetBrains/kotlin/stargazers"),
            Arguments.of("https://github.com/JetBrains/kotlin/pulls"),
            Arguments.of("https://github.com/JetBrains/kotlin/issues"),
            Arguments.of("https://stackoverflow.com/a/32872406"),
            Arguments.of("https://stackoverflow.com/questions/24840667#32872406")
        );
    }
}
