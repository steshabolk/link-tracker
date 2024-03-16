package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.CommandType;
import edu.java.bot.handler.LinkHandler;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.BotSendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UntrackCommand implements Command, Reply {

    private final CommandType commandType = CommandType.UNTRACK;
    private final LinkHandler linkHandler;
    private final ScrapperService scrapperService;

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public boolean isTriggered(Update update) {
        return Command.super.isTriggered(update) || isReply(update, BotReply.UNTRACK_COMMAND.getReply());
    }

    @Override
    public SendMessage handle(Update update) {
        if (Command.super.isTriggered(update)) {
            return BotSendMessage.getSendMessage(
                update.message().chat().id(),
                BotReply.UNTRACK_COMMAND.getReply(),
                true
            );
        }
        return handleReply(update);
    }

    @Override
    public SendMessage handleReply(Update update) {
        return linkHandler.handleLink(update, scrapperService::removeLink, BotReply.LINK_REMOVED.getReply());
    }
}
