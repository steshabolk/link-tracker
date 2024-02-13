package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.dto.LinkDto;
import edu.java.bot.enums.LinkType;
import edu.java.bot.exception.ApiException;
import edu.java.bot.sender.BotSender;
import edu.java.bot.service.ScrapperService;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LinkHandlerImplTest {

    @InjectMocks
    private LinkHandlerImpl linkHandler;
    @Spy
    private BotSender sender;
    @Mock
    private ScrapperService scrapperService;
    @Mock
    private Update update;
    @Mock
    private Message message;
    @Mock
    private Chat chat;
    @Captor
    private ArgumentCaptor<LinkDto> linkDtoCaptor;

    @Nested
    class HandleLinkTest {

        @Test
        void shouldReturnSuccessWhenStartTrackingLink() {
            String link = "https://github.com/JetBrains/kotlin";
            doReturn(message).when(update).message();
            doReturn(link).when(message).text();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = linkHandler.handleLink(update, scrapperService::track, "success");

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo("success");

            verify(scrapperService).track(anyLong(), linkDtoCaptor.capture());
            LinkDto linkDto = linkDtoCaptor.getValue();
            assertThat(linkDto.linkType()).isEqualTo(LinkType.GITHUB);
            assertThat(linkDto.uri().toString()).isEqualTo("https://github.com/JetBrains/kotlin");
        }

        @Test
        void shouldReturnSuccessWhenStopTrackingLink() {
            String link = "https://stackoverflow.com/questions/tagged/java";
            doReturn(message).when(update).message();
            doReturn(link).when(message).text();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = linkHandler.handleLink(update, scrapperService::untrack, "success");

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo("success");

            verify(scrapperService).untrack(anyLong(), linkDtoCaptor.capture());
            LinkDto linkDto = linkDtoCaptor.getValue();
            assertThat(linkDto.linkType()).isEqualTo(LinkType.STACKOVERFLOW);
            assertThat(linkDto.uri().toString()).isEqualTo("https://stackoverflow.com/questions/tagged/java");
        }
    }

    @Nested
    class ParseLinkTest {

        @ParameterizedTest
        @MethodSource("edu.java.bot.handler.LinkHandlerImplTest#validLink")
        void shouldReturnParsedLinkWhenLinkIsValid(LinkType linkType, String url) {
            LinkDto linkDto = linkHandler.parseLink(url);

            assertThat(linkDto.linkType()).isEqualTo(linkType);
            assertThat(linkDto.uri().toString()).isEqualTo(url);
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.handler.LinkHandlerImplTest#invalidLink")
        void shouldThrowExceptionWhenLinkIsInvalid(String url) {
            String expected = ":heavy_multiplication_x: your link is invalid. please try again";

            assertThatThrownBy(() -> linkHandler.parseLink(url))
                .isInstanceOf(ApiException.class)
                .hasMessage(expected);
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.handler.LinkHandlerImplTest#unsupportedLink")
        void shouldThrowExceptionWhenLinkIsUnsupported(String url) {
            String expected = ":heavy_multiplication_x: sorry, tracking is not supported on this resource";

            assertThatThrownBy(() -> linkHandler.parseLink(url))
                .isInstanceOf(ApiException.class)
                .hasMessage(expected);
        }
    }

    static Stream<Arguments> validLink() {
        return Stream.of(
            Arguments.of(LinkType.GITHUB, "https://github.com/JetBrains/kotlin"),
            Arguments.of(LinkType.STACKOVERFLOW, "https://stackoverflow.com/questions/tagged/java")
        );
    }

    static Stream<Arguments> invalidLink() {
        return Stream.of(
            Arguments.of(""),
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
