package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.handler.LinkHandler;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.BotSendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommand implements Command, Reply {

    private final CommandType commandType = CommandType.UNTRACK;
    private static final String REPLY =
        String.format("%s send a link to stop tracking", Emoji.LINK.getMarkdown());
    private static final String SUCCESS_REPLY =
        String.format("%s the link has been removed", Emoji.CHECK.getMarkdown());
    private final LinkHandler linkHandler;
    private final ScrapperService scrapperService;

    @Autowired
    public UntrackCommand(LinkHandler linkHandler, ScrapperService scrapperService) {
        this.linkHandler = linkHandler;
        this.scrapperService = scrapperService;
    }

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public boolean isTriggered(Update update) {
        return Command.super.isTriggered(update) || isReply(update, REPLY);
    }

    @Override
    public SendMessage handle(Update update) {
        if (Command.super.isTriggered(update)) {
            return BotSendMessage.getSendMessage(update.message().chat().id(), REPLY, true);
        }
        return handleReply(update);
    }

    @Override
    public SendMessage handleReply(Update update) {
        return linkHandler.handleLink(update, scrapperService::untrack, SUCCESS_REPLY);
    }
}
