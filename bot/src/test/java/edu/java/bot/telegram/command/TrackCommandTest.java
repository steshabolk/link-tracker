package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.enums.CommandType;
import edu.java.bot.handler.LinkHandlerImpl;
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
class TrackCommandTest {

    @InjectMocks
    private TrackCommand trackCommand;
    @Mock
    private ScrapperService scrapperService;
    @Spy
    private BotSender sender;
    @Mock
    private LinkHandlerImpl linkHandler;
    @Mock
    private Update update;
    @Mock
    private Message message;
    @Mock
    private Message reply;
    @Mock
    private Chat chat;

    @Nested
    class CommandTypeTest {

        @Test
        void testCommandType() {
            CommandType commandType = trackCommand.commandType();
            assertThat(commandType).isEqualTo(CommandType.TRACK);
        }
    }

    @Nested
    class CommandTest {

        @Test
        void testCommand() {
            String command = trackCommand.command();
            assertThat(command).isEqualTo("/track");
        }
    }

    @Nested
    class DescriptionTest {

        @Test
        void testDescription() {
            String description = trackCommand.description();
            assertThat(description).isEqualTo("start tracking a link");
        }
    }

    @Nested
    class IsTriggeredTest {

        @Test
        void shouldReturnTrueWhenCommandIsCorrect() {
            doReturn(message).when(update).message();
            doReturn("/track").when(message).text();

            boolean isTriggered = trackCommand.isTriggered(update);

            assertThat(isTriggered).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCommandIsIncorrect() {
            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();

            boolean isTriggered = trackCommand.isTriggered(update);

            assertThat(isTriggered).isFalse();
        }

        @Test
        void shouldReturnTrueWhenItIsCommandReply() {
            String expectedReply = EmojiParser.parseToUnicode(
                ":link: send a link to start tracking\n"
                    + "◉ github.com\n"
                    + "◉ stackoverflow.com");

            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();
            doReturn(reply).when(message).replyToMessage();
            doReturn(expectedReply).when(reply).text();

            boolean isTriggered = trackCommand.isTriggered(update);

            assertThat(isTriggered).isTrue();
        }

        @Test
        void shouldReturnFalseWhenCommandIsIncorrectAndItIsNotCommandReply() {
            String dummyReply = "dummy";

            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();
            doReturn(reply).when(message).replyToMessage();
            doReturn(dummyReply).when(reply).text();

            boolean isTriggered = trackCommand.isTriggered(update);

            assertThat(isTriggered).isFalse();
        }
    }

    @Nested
    class HandleTest {

        @Test
        void shouldReturnReplyWhenCommandIsTriggered() {
            String expectedReply = EmojiParser.parseToUnicode(
                ":link: send a link to start tracking\n"
                    + "◉ github.com\n"
                    + "◉ stackoverflow.com");

            doReturn(message).when(update).message();
            doReturn("/track").when(message).text();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = trackCommand.handle(update);

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
            assertThat(parameters.get("reply_markup")).isInstanceOf(ForceReply.class);
        }
    }
}
