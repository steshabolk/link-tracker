package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import edu.java.bot.sender.BotSender;
import edu.java.bot.service.ScrapperService;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListCommand implements Command {

    private final CommandType commandType = CommandType.LIST;
    private final String emptyListReply =
        String.format(
            "%s your list of tracked links is empty\n%s",
            Emoji.BOOKMARK.getMarkdown(),
            CommandType.TRACK.getCommandBulletPoint()
        );
    private final BotSender sender;
    private final ScrapperService scrapperService;

    @Autowired
    public ListCommand(BotSender sender, ScrapperService scrapperService) {
        this.sender = sender;
        this.scrapperService = scrapperService;
    }

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        Map<LinkType, List<URI>> links = scrapperService.getLinks(chatId);
        if (links.isEmpty()) {
            return sender.getSendMessage(chatId, emptyListReply);
        }
        String linksReply = links.entrySet().stream()
            .map(entry -> String.format("%s *%s*\n", Emoji.LINK.getMarkdown(), entry.getKey())
                + entry.getValue().stream()
                .map(link -> String.format("◉ %s", link))
                .collect(Collectors.joining("\n")))
            .collect(Collectors.joining("\n\n"));
        return sender.getSendMessage(chatId, linksReply);
    }
}
