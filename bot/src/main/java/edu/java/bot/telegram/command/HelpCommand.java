package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.util.BotSendMessage;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    private final CommandType commandType = CommandType.HELP;
    private static String reply = String.format("%s select one of the available commands:\n", Emoji.INFO.getMarkdown());

    public HelpCommand() {
        reply += Stream.of(CommandType.TRACK, CommandType.UNTRACK, CommandType.LIST)
            .map(CommandType::getCommandBulletPoint)
            .collect(Collectors.joining("\n"));
    }

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public SendMessage handle(Update update) {
        return BotSendMessage.getSendMessage(update.message().chat().id(), reply);
    }
}
