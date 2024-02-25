package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.dto.LinkDto;
import edu.java.bot.enums.LinkType;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.LinkParser;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LinkHandlerTest {

    @InjectMocks
    private LinkHandler linkHandler;
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
            try (MockedStatic<LinkParser> parserMock = mockStatic(LinkParser.class)) {
                parserMock.when(() -> LinkParser.parseLink(link))
                    .thenReturn(new LinkDto(LinkType.GITHUB, URI.create("https://github.com/JetBrains/kotlin")));
            }

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
            try (MockedStatic<LinkParser> parserMock = mockStatic(LinkParser.class)) {
                parserMock.when(() -> LinkParser.parseLink(link))
                    .thenReturn(new LinkDto(LinkType.GITHUB, URI.create("https://github.com/JetBrains/kotlin")));
            }

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
}
