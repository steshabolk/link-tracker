package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.sender.BotSender;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    private final CommandType commandType = CommandType.HELP;
    private String reply = String.format("%s select one of the available commands:\n", Emoji.INFO.getMarkdown());
    private final BotSender sender;

    @Autowired
    public HelpCommand(BotSender sender) {
        this.sender = sender;
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
        return sender.getSendMessage(update.message().chat().id(), reply);
    }
}
