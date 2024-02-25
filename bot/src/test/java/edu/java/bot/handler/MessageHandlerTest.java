package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.telegram.command.Command;
import edu.java.bot.telegram.command.HelpCommand;
import edu.java.bot.telegram.command.ListCommand;
import edu.java.bot.telegram.command.StartCommand;
import edu.java.bot.telegram.command.TrackCommand;
import edu.java.bot.telegram.command.UntrackCommand;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageHandlerTest {

    private MessageHandler messageHandler;
    @Mock
    private HelpCommand helpCommand;
    @Mock
    private ListCommand listCommand;
    @Mock
    private StartCommand startCommand;
    @Mock
    private TrackCommand trackCommand;
    @Mock
    private UntrackCommand untrackCommand;
    @Mock
    private Update update;
    @Mock
    private Message message;
    @Mock
    private Chat chat;
    @Mock
    private SendMessage sendMessage;

    @BeforeEach
    void init() {
        List<Command> commands = List.of(helpCommand, listCommand, startCommand, trackCommand, untrackCommand);
        messageHandler = new MessageHandler(commands);
        doReturn(false).when(helpCommand).isTriggered(update);
        doReturn(false).when(listCommand).isTriggered(update);
        doReturn(false).when(trackCommand).isTriggered(update);
        doReturn(false).when(untrackCommand).isTriggered(update);
    }

    @Nested
    class HandleTest {

        @Test
        void shouldReturnCommandReplyWhenOneCommandIsTriggerred() {
            doReturn(message).when(update).message();
            doReturn("/start").when(message).text();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            doReturn(true).when(startCommand).isTriggered(update);
            doReturn(sendMessage).when(startCommand).handle(update);

            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNotNull().isEqualTo(sendMessage);
        }

        @Test
        void shouldReturnUnknownReplyWhenNoCommandIsTriggerred() {
            String expectedReply = EmojiParser.parseToUnicode(
                ":heavy_multiplication_x: sorry, unable to process an unknown command\n"
                    + "◉ */help* ➜ show commands");

            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            doReturn(false).when(startCommand).isTriggered(update);

            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNotNull();
            Map<String, Object> parameters = actual.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
        }

        @Test
        void shouldReturnNullWhenMessageIsNull() {
            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNull();
        }

        @Test
        void shouldReturnNullWhenMessageTextIsNull() {
            doReturn(message).when(update).message();

            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNull();
        }
    }
}
