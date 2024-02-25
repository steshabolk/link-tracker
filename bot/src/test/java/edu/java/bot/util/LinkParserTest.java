package edu.java.bot.util;

import edu.java.bot.dto.LinkDto;
import edu.java.bot.enums.LinkType;
import edu.java.bot.exception.ApiException;
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
        void shouldReturnParsedLinkWhenLinkIsValid(LinkType linkType, String url) {
            LinkDto linkDto = LinkParser.parseLink(url);

            assertThat(linkDto.linkType()).isEqualTo(linkType);
            assertThat(linkDto.uri().toString()).isEqualTo(url);
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.util.LinkParserTest#invalidLink")
        void shouldThrowExceptionWhenLinkIsInvalid(String url) {
            String expected = ":heavy_multiplication_x: your link is invalid. please try again";

            assertThatThrownBy(() -> LinkParser.parseLink(url))
                .isInstanceOf(ApiException.class)
                .hasMessage(expected);
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.util.LinkParserTest#unsupportedLink")
        void shouldThrowExceptionWhenLinkIsUnsupported(String url) {
            String expected = ":heavy_multiplication_x: sorry, tracking is not supported on this resource";

            assertThatThrownBy(() -> LinkParser.parseLink(url))
                .isInstanceOf(ApiException.class)
                .hasMessage(expected);
        }
    }

    static Stream<Arguments> validLink() {
        return Stream.of(
            Arguments.of(LinkType.GITHUB, "https://github.com/JetBrains/kotlin"),
            Arguments.of(LinkType.STACKOVERFLOW, "https://stackoverflow.com/questions/24840667")
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
            Arguments.of("https://leetcode.com/problemset/algorithms/")
        );
    }
}
