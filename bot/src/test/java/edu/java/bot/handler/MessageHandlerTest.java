package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMemberUpdated;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.telegram.command.Command;
import edu.java.bot.telegram.command.HelpCommand;
import edu.java.bot.telegram.command.ListCommand;
import edu.java.bot.telegram.command.StartCommand;
import edu.java.bot.telegram.command.TrackCommand;
import edu.java.bot.telegram.command.UntrackCommand;
import java.util.List;
import java.util.Map;
import io.micrometer.core.instrument.Counter;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageHandlerTest {

    private MessageHandler messageHandler;
    @Mock
    private ScrapperService scrapperService;
    @Mock
    private ClientExceptionHandler clientExceptionHandler;
    @Mock
    private Counter counter;
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
    @Mock
    private ChatMemberUpdated chatMemberUpdated;
    @Mock
    private ChatMember chatMember;

    @BeforeEach
    void init() {
        List<Command> commands = List.of(helpCommand, listCommand, startCommand, trackCommand, untrackCommand);
        messageHandler = new MessageHandler(commands, scrapperService, clientExceptionHandler, counter);
        doReturn(false).when(helpCommand).isTriggered(update);
        doReturn(false).when(listCommand).isTriggered(update);
        doReturn(false).when(trackCommand).isTriggered(update);
        doReturn(false).when(untrackCommand).isTriggered(update);
        doReturn(true).when(startCommand).isTriggered(update);
        doReturn(message).when(update).message();
        doReturn("/start").when(message).text();
        doReturn(chat).when(message).chat();
        doReturn(1L).when(chat).id();
    }

    @Nested
    class HandleCommandTest {

        @Test
        void shouldReturnCommandReplyWhenOneCommandIsTriggerred() {
            doReturn(sendMessage).when(startCommand).handle(update);

            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNotNull().isEqualTo(sendMessage);
        }

        @Test
        void shouldReturnUnknownReplyWhenNoCommandIsTriggerred() {
            String expectedReply = EmojiParser.parseToUnicode(
                ":heavy_multiplication_x: sorry, unable to process an unknown command\n"
                    + "➜ <b>/help</b> - show commands");

            doReturn(false).when(startCommand).isTriggered(update);

            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNotNull();
            Map<String, Object> parameters = actual.getParameters();
            assertThat(parameters.get("chat_id")).isEqualTo(1L);
            assertThat(parameters.get("text")).isEqualTo(expectedReply);
        }

        @Test
        void shouldDeleteChatWhenBotIsBlocked() {
            doReturn(message).when(update).message();
            doReturn(null).when(message).text();
            doReturn(chatMemberUpdated).when(update).myChatMember();
            doReturn(chat).when(chatMemberUpdated).chat();
            doReturn(chatMember).when(chatMemberUpdated).newChatMember();
            doReturn(ChatMember.Status.kicked).when(chatMember).status();

            SendMessage actual = messageHandler.handle(update);

            assertThat(actual).isNull();
            verify(scrapperService).deleteChat(1L);
        }
    }
}
