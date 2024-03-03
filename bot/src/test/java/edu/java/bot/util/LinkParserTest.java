package edu.java.bot.util;

import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class LinkParserTest {

    @Nested
    class ParseLinkTest {

        @ParameterizedTest
        @MethodSource("edu.java.bot.util.LinkParserTest#validLink")
        void shouldReturnLinkWhenLinkIsValid(String url) {
            URI parsedLink = LinkParser.parseLink(url);

            assertThat(parsedLink.toString()).isEqualTo(url);
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.util.LinkParserTest#invalidLink")
        void shouldThrowExceptionWhenLinkIsInvalid(String url) {
            String expected = ":heavy_multiplication_x: your link is invalid. please try again";

            assertThatThrownBy(() -> LinkParser.parseLink(url))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expected);
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.util.LinkParserTest#notSupportedLink")
        void shouldThrowExceptionWhenLinkIsNotSupported(String url) {
            String expected = ":heavy_multiplication_x: sorry, tracking is not supported on this resource";

            assertThatThrownBy(() -> LinkParser.parseLink(url))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expected);
        }
    }

    static Stream<Arguments> validLink() {
        return Stream.of(
            Arguments.of("https://github.com/JetBrains/kotlin"),
            Arguments.of("https://github.com/JetBrains/kotlin/tree/branch-name"),
            Arguments.of("https://github.com/JetBrains/kotlin/pull/1"),
            Arguments.of("https://github.com/JetBrains/kotlin/issues/1"),
            Arguments.of("https://stackoverflow.com/questions/24840667")
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

    static Stream<Arguments> notSupportedLink() {
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
