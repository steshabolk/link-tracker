package edu.java.bot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.exception.ApiErrorResponse;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClientExceptionHandlerTest {

    private ClientExceptionHandler clientExceptionHandler;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void init() {
        clientExceptionHandler = new ClientExceptionHandler(mapper);
    }

    @Nested
    class HandleClientExceptionTest {

        @SneakyThrows
        @Test
        void shouldReturnReplyWhenChatNotFound() {
            ApiErrorResponse apiResponse =
                new ApiErrorResponse(null, "CHAT_NOT_FOUND", null, null, List.of());
            WebClientResponseException ex = new WebClientResponseException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                null,
                mapper.writeValueAsBytes(apiResponse),
                null
            );

            String expectedReply = "➜ <b>/start</b> - start the bot";

            SendMessage actual = clientExceptionHandler.getReplyForScrapperErrorResponse(ex, 1L);

            assertThat(actual.getParameters().get("text")).isEqualTo(expectedReply);
        }

        @SneakyThrows
        @Test
        void shouldReturnReplyWhenLinkNotFound() {
            ApiErrorResponse apiResponse =
                new ApiErrorResponse(null, "LINK_NOT_FOUND", null, null, List.of());
            WebClientResponseException ex = new WebClientResponseException(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                null,
                mapper.writeValueAsBytes(apiResponse),
                null
            );

            String expectedReply = EmojiParser.parseToUnicode(
                ":heavy_multiplication_x: unknown link\n" +
                    "➜ <b>/list</b> - show a list of tracked links"
            );

            SendMessage actual = clientExceptionHandler.getReplyForScrapperErrorResponse(ex, 1L);

            assertThat(actual.getParameters().get("text")).isEqualTo(expectedReply);
        }

        @SneakyThrows
        @Test
        void shouldReturnReplyWhenLinkExists() {
            ApiErrorResponse apiResponse =
                new ApiErrorResponse(null, "LINK_ALREADY_EXISTS", null, null, List.of());
            WebClientResponseException ex = new WebClientResponseException(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                null,
                mapper.writeValueAsBytes(apiResponse),
                null
            );

            String expectedReply = EmojiParser.parseToUnicode(
                ":heavy_check_mark: link has already been added\n" +
                    "➜ <b>/list</b> - show a list of tracked links"
            );

            SendMessage actual = clientExceptionHandler.getReplyForScrapperErrorResponse(ex, 1L);

            assertThat(actual.getParameters().get("text")).isEqualTo(expectedReply);
        }

        @SneakyThrows
        @Test
        void shouldReturnReplyWhenLinkIsInvalid() {
            ApiErrorResponse apiResponse =
                new ApiErrorResponse(null, "INVALID_LINK", null, null, List.of());
            WebClientResponseException ex = new WebClientResponseException(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                null,
                mapper.writeValueAsBytes(apiResponse),
                null
            );

            String expectedReply =
                EmojiParser.parseToUnicode(":heavy_multiplication_x: your link is invalid. please try again");

            SendMessage actual = clientExceptionHandler.getReplyForScrapperErrorResponse(ex, 1L);

            assertThat(actual.getParameters().get("text")).isEqualTo(expectedReply);
        }

        @SneakyThrows
        @Test
        void shouldReturnReplyWhenLinkNotSupported() {
            ApiErrorResponse apiResponse =
                new ApiErrorResponse(null, "NOT_SUPPORTED_SOURCE", null, null, List.of());
            WebClientResponseException ex = new WebClientResponseException(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                null,
                mapper.writeValueAsBytes(apiResponse),
                null
            );

            String expectedReply =
                EmojiParser.parseToUnicode(":heavy_multiplication_x: sorry, tracking is not supported on this resource");

            SendMessage actual = clientExceptionHandler.getReplyForScrapperErrorResponse(ex, 1L);

            assertThat(actual.getParameters().get("text")).isEqualTo(expectedReply);
        }
    }
}
