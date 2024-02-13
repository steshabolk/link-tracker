package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.enums.CommandType;
import edu.java.bot.sender.BotSender;
import edu.java.bot.service.ScrapperService;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

    @InjectMocks
    private StartCommand startCommand;
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
    @Mock
    private User user;

    @Nested
    class CommandTypeTest {

        @Test
        void testCommandType() {
            CommandType commandType = startCommand.commandType();
            assertThat(commandType).isEqualTo(CommandType.START);
        }
    }

    @Nested
    class CommandTest {

        @Test
        void testCommand() {
            String command = startCommand.command();
            assertThat(command).isEqualTo("/start");
        }
    }

    @Nested
    class DescriptionTest {

        @Test
        void testDescription() {
            String description = startCommand.description();
            assertThat(description).isEqualTo("start the bot");
        }
    }

    @Nested
    class IsTriggeredTest {

        @Test
        void shouldReturnTrueWhenCommandIsCorrect() {
            doReturn(message).when(update).message();
            doReturn("/start").when(message).text();

            boolean isTriggered = startCommand.isTriggered(update);

            assertThat(isTriggered).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCommandIsIncorrect() {
            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();

            boolean isTriggered = startCommand.isTriggered(update);

            assertThat(isTriggered).isFalse();
        }
    }

    @Nested
    class HandleTest {

        @Test
        void testCommandHandling() {
            String expectedReply = EmojiParser.parseToUnicode(
                "*hi!* :wave:\n"
                    + "this is a link tracking bot :robot_face:\n"
                    + "◉ */help* ➜ show commands");

            doReturn(message).when(update).message();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();
            doReturn(user).when(message).from();
            doReturn(2L).when(user).id();

            SendMessage sendMessage = startCommand.handle(update);

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
        }
    }
}
