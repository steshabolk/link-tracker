package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.CommandType;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.BotSendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartCommand implements Command {

    private final CommandType commandType = CommandType.START;
    private final ScrapperService scrapperService;

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        scrapperService.registerChat(chatId);
        return BotSendMessage.getSendMessage(chatId, BotReply.START_COMMAND.getReply());
    }
}
