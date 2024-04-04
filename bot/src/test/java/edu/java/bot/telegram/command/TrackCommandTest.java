package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.enums.CommandType;
import edu.java.bot.handler.LinkHandler;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.LinkSourceUtil;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class TrackCommandTest {

    @InjectMocks
    private TrackCommand trackCommand;
    @Mock
    private ScrapperService scrapperService;
    @Mock
    private LinkHandler linkHandler;
    @Mock
    private Update update;
    @Mock
    private Message message;
    @Mock
    private Message reply;
    @Mock
    private Chat chat;
    private static MockedStatic<LinkSourceUtil> linkSourceUtilMock;

    @BeforeAll
    public static void init() {
        linkSourceUtilMock = mockStatic(LinkSourceUtil.class);
    }

    @AfterAll
    public static void close() {
        linkSourceUtilMock.close();
    }

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
        void shouldReturnFalseWhenCommandIsIncorrectAndItIsNotCommandReply() {
            doReturn(message).when(update).message();
            doReturn("/dummy").when(message).text();
            doReturn(reply).when(message).replyToMessage();
            doReturn("dummy").when(reply).text();

            boolean isTriggered = trackCommand.isTriggered(update);

            assertThat(isTriggered).isFalse();
        }
    }

    @Nested
    class HandleTest {

        @Test
        void shouldReturnReplyWhenCommandIsTriggered() {
            String expectedReply = EmojiParser.parseToUnicode(":link: send a link to start tracking");

            doReturn(message).when(update).message();
            doReturn("/track").when(message).text();
            doReturn(chat).when(message).chat();
            doReturn(1L).when(chat).id();

            SendMessage sendMessage = trackCommand.handle(update);

            Map<String, Object> parameters = sendMessage.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text").toString()).startsWith(expectedReply);
            assertThat(parameters.get("reply_markup")).isInstanceOf(ForceReply.class);
        }
    }
}
