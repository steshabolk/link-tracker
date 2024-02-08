package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import edu.java.bot.handler.LinkHandlerImpl;
import edu.java.bot.sender.BotSender;
import edu.java.bot.service.ScrapperService;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackCommand implements Command, Reply {

    private final CommandType commandType = CommandType.TRACK;
    private final String reply =
        String.format("%s send a link to start tracking\n%s", Emoji.LINK.getMarkdown(),
            Arrays.stream(LinkType.values())
                .map(LinkType::getCommandBulletPoint)
                .collect(Collectors.joining("\n"))
        );
    private final String successReply = String.format("%s the link has been added", Emoji.CHECK.getMarkdown());
    private final BotSender sender;
    private final LinkHandlerImpl linkHandler;
    private final ScrapperService scrapperService;

    @Autowired
    public TrackCommand(BotSender sender, LinkHandlerImpl linkHandler, ScrapperService scrapperService) {
        this.sender = sender;
        this.linkHandler = linkHandler;
        this.scrapperService = scrapperService;
    }

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public boolean isTriggered(Update update) {
        return Command.super.isTriggered(update) || isReply(update, reply);
    }

    @Override
    public SendMessage handle(Update update) {
        if (Command.super.isTriggered(update)) {
            return sender.getSendMessage(update.message().chat().id(), reply, true);
        }
        return handleReply(update);
    }

    @Override
    public SendMessage handleReply(Update update) {
        return linkHandler.handleLink(update, scrapperService::track, successReply);
    }
}
