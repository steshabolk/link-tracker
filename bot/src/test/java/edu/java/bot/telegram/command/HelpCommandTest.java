package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.enums.CommandType;
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
class HelpCommandTest {

    @InjectMocks
    private HelpCommand helpCommand;
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
            CommandType commandType = helpCommand.commandType();
            assertThat(commandType).isEqualTo(CommandType.HELP);
        }
    }

    @Nested
    class CommandTest {

        @Test
        void testCommand() {
            String command = helpCommand.command();
            assertThat(command).isEqualTo("/help");
        }
    }

    @Nested
    class DescriptionTest {

        @Test
        void testDescription() {
            String description = helpCommand.description();
            assertThat(description).isEqualTo("show commands");
        }
    }

    @Nested
    class IsTriggeredTest {

        @Test
        void shouldReturnTrueWhenCommandIsCorrect() {
            doReturn(message).when(update).message();
            doReturn("/help").when(message).text();

            boolean isTriggered = helpCommand.isTriggered(update);

            assertThat(isTriggered).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCommandIsIncorrect() {
            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();

            boolean isTriggered = helpCommand.isTriggered(update);

            assertThat(isTriggered).isFalse();
        }
    }

    @Nested
    class HandleTest {

        @Test
        void testCommandHandling() {
            String expectedReply = EmojiParser.parseToUnicode(
                ":information_source: select one of the available commands:\n"
                    + "◉ */track* ➜ start tracking a link\n"
                    + "◉ */untrack* ➜ stop tracking a link\n"
                    + "◉ */list* ➜ show a list of tracked links");
            doReturn(message).when(update).message();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = helpCommand.handle(update);

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
        }
    }
}
