package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.CommandType;
import edu.java.bot.util.BotSendMessage;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    @Override
    public CommandType commandType() {
        return CommandType.HELP;
    }

    @Override
    public SendMessage handle(Update update) {
        return BotSendMessage.getSendMessage(update.message().chat().id(), BotReply.HELP_COMMAND.getReply());
    }
}
