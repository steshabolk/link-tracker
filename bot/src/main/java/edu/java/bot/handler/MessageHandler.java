package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.telegram.command.Command;
import edu.java.bot.util.BotSendMessage;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class MessageHandler {

    private static final String UNKNOWN_COMMAND_REPLY =
        String.format(
            "%s sorry, unable to process an unknown command\n%s",
            Emoji.ERROR.getMarkdown(),
            CommandType.HELP.getCommandBulletPoint()
        );
    private final List<Command> commands;

    @Autowired
    public MessageHandler(List<Command> commands) {
        this.commands = commands;
    }

    public SendMessage handle(Update update) {
        Long chatId = Optional.ofNullable(update.message())
            .filter(message -> StringUtils.hasText(message.text()))
            .map(Message::chat)
            .map(Chat::id)
            .orElse(null);
        if (chatId == null) {
            log.warn("error processing update: chat id is null");
            return null;
        }
        Optional<Command> triggeredCommand = commands.stream()
            .filter(cmd -> cmd.isTriggered(update))
            .findFirst();
        return triggeredCommand
            .map(cmd -> cmd.handle(update))
            .orElseGet(() -> BotSendMessage.getSendMessage(chatId, UNKNOWN_COMMAND_REPLY));
    }
}
