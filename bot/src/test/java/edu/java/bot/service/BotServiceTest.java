package edu.java.bot.service;

import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.dto.request.LinkUpdate;
import edu.java.bot.listener.BotListener;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @InjectMocks
    private BotService botService;
    @Mock
    private BotListener botListener;
    @Mock
    private ScrapperService scrapperService;
    @Mock
    private SendResponse sendResponse;
    @Captor
    private ArgumentCaptor<SendMessage> responseCaptor;

    @Nested
    class SendUpdateTest {

        @Test
        void successfulSendUpdate() {
            LinkUpdate update = new LinkUpdate(
                1L,
                URI.create("https://github.com/JetBrains/kotlin"),
                "new update",
                List.of(2L)
            );
            String expectedResponse = EmojiParser.parseToUnicode(
                ":link: https://github.com/JetBrains/kotlin\nnew update");
            doReturn(sendResponse).when(botListener).execute(any());
            doReturn(true).when(sendResponse).isOk();

            botService.sendLinkUpdate(update);

            verify(botListener).execute(responseCaptor.capture());
            verify(scrapperService, never()).deleteChat(anyLong());
            Map<String, Object> responseParameters = responseCaptor.getValue().getParameters();
            assertThat(responseParameters.get("chat_id")).isEqualTo(2L);
            assertThat(responseParameters.get("text")).isEqualTo(expectedResponse);
        }

        @Test
        void shouldDeleteChatWhenResponseCode403() {
            LinkUpdate update = new LinkUpdate(
                1L,
                URI.create("https://github.com/JetBrains/kotlin"),
                "new update",
                List.of(2L)
            );
            doReturn(sendResponse).when(botListener).execute(any());
            doReturn(false).when(sendResponse).isOk();
            doReturn(403).when(sendResponse).errorCode();

            botService.sendLinkUpdate(update);

            verify(botListener).execute(any());
            verify(scrapperService).deleteChat(2L);
        }
    }
}
