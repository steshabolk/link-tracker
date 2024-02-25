package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.LinkType;
import edu.java.bot.service.ScrapperService;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {

    @InjectMocks
    private ListCommand listCommand;
    @Mock
    private ScrapperService scrapperService;
    @Mock
    private Update update;
    @Mock
    private Message message;
    @Mock
    private Chat chat;

    @Nested
    class CommandTypeTest {

        @Test
        void testCommandType() {
            CommandType commandType = listCommand.commandType();
            assertThat(commandType).isEqualTo(CommandType.LIST);
        }
    }

    @Nested
    class CommandTest {

        @Test
        void testCommand() {
            String command = listCommand.command();
            assertThat(command).isEqualTo("/list");
        }
    }

    @Nested
    class DescriptionTest {

        @Test
        void testDescription() {
            String description = listCommand.description();
            assertThat(description).isEqualTo("show a list of tracked links");
        }
    }

    @Nested
    class IsTriggeredTest {

        @Test
        void shouldReturnTrueWhenCommandIsCorrect() {
            doReturn(message).when(update).message();
            doReturn("/list").when(message).text();

            boolean isTriggered = listCommand.isTriggered(update);

            assertThat(isTriggered).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCommandIsIncorrect() {
            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();

            boolean isTriggered = listCommand.isTriggered(update);

            assertThat(isTriggered).isFalse();
        }
    }

    @Nested
    class HandleTest {

        @Test
        void shouldReturnEmptyListReplyWhenListOfLinksIsEmpty() {
            String expectedReply = EmojiParser.parseToUnicode(
                ":bookmark_tabs: your list of tracked links is empty\n"
                    + "◉ */track* ➜ start tracking a link");

            doReturn(Map.of()).when(scrapperService).getLinks(1L);
            doReturn(message).when(update).message();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = listCommand.handle(update);

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
        }

        @Test
        void shouldReturnLinksWhenListOfLinksIsNotEmpty() {
            URI githubUri = URI.create("https://github.com/JetBrains/kotlin");
            URI stackoverflowUri = URI.create("https://stackoverflow.com/questions/tagged/java");
            Map<LinkType, List<URI>> links = new LinkedHashMap<>();
            links.put(LinkType.GITHUB, List.of(githubUri));
            links.put(LinkType.STACKOVERFLOW, List.of(stackoverflowUri));

            String expectedReply = EmojiParser.parseToUnicode(
                ":link: *GITHUB*\n"
                    + "◉ https://github.com/JetBrains/kotlin\n"
                    + "\n" +
                    ":link: *STACKOVERFLOW*\n"
                    + "◉ https://stackoverflow.com/questions/tagged/java");

            doReturn(links).when(scrapperService).getLinks(1L);
            doReturn(message).when(update).message();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = listCommand.handle(update);

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
        }
    }
}
