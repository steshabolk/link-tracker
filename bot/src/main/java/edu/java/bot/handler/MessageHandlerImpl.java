package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.sender.BotSender;
import edu.java.bot.telegram.command.Command;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageHandlerImpl implements MessageHandler {

    private final String unknownCommandReply =
        String.format(
            "%s sorry, unable to process an unknown command\n%s",
            Emoji.ERROR.getMarkdown(),
            CommandType.HELP.getCommandBulletPoint()
        );
    private final List<Command> commands;
    private final BotSender sender;

    public MessageHandlerImpl(List<Command> commands, BotSender sender) {
        this.commands = commands;
        this.sender = sender;
    }

    @Override
    public List<Command> commands() {
        return commands;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = null;
        if (update.message() != null && update.message().text() != null) {
            chatId = update.message().chat().id();
            List<Command> command = commands.stream().filter(cmd -> cmd.isTriggered(update)).toList();
            if (command.size() == 1) {
                return command.get(0).handle(update);
            }
        }
        if (chatId == null) {
            log.warn("error processing update: chat id is null");
            return null;
        }
        return sender.getSendMessage(chatId, unknownCommandReply);
    }
}
