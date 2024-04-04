package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.BotSendMessage;
import edu.java.bot.util.LinkSourceUtil;
import edu.java.bot.util.TextUtil;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListCommand implements Command {

    private final ScrapperService scrapperService;

    @Override
    public CommandType commandType() {
        return CommandType.LIST;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        List<URI> links = scrapperService.getLinks(chatId);
        if (links.isEmpty()) {
            return BotSendMessage.getSendMessage(chatId, BotReply.EMPTY_LIST.getReply());
        }
        Map<String, List<URI>> groupedLinks = groupByLinkSource(links);
        String linksReply = getLinksReply(groupedLinks);
        return BotSendMessage.getSendMessage(chatId, linksReply);
    }

    private Map<String, List<URI>> groupByLinkSource(List<URI> links) {
        return links.stream()
            .collect(Collectors.groupingBy(it -> LinkSourceUtil.getLinkType(it.getHost())))
            .entrySet().stream()
            .filter(e -> e.getKey().isPresent())
            .collect(Collectors.toMap(e -> e.getKey().get(), Map.Entry::getValue));
    }

    private String getLinksReply(Map<String, List<URI>> links) {
        return links.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> getLinkSourceResponse(e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n\n"));
    }

    private String getLinkSourceResponse(String linkType, List<URI> links) {
        return String.format("%s %s\n", Emoji.LINK.toUnicode(), TextUtil.toBold(linkType.toUpperCase()))
            + links.stream()
            .map(link -> String.format("➜ %s", link))
            .collect(Collectors.joining("\n"));
    }
}
