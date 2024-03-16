package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.BotSendMessage;
import edu.java.bot.util.LinkTypeUtil;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListCommand implements Command {

    private final CommandType commandType = CommandType.LIST;
    private final ScrapperService scrapperService;

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        List<URI> links = scrapperService.getLinks(chatId);
        if (links.isEmpty()) {
            return BotSendMessage.getSendMessage(chatId, BotReply.EMPTY_LIST.getReply());
        }
        Map<LinkType, List<URI>> groupedLinks = groupByLinkType(links);
        String linksReply = getLinksReply(groupedLinks);
        return BotSendMessage.getSendMessage(chatId, linksReply);
    }

    private Map<LinkType, List<URI>> groupByLinkType(List<URI> links) {
        return links.stream()
            .collect(Collectors.groupingBy(it -> LinkTypeUtil.getLinkType(it.getHost())))
            .entrySet().stream()
            .filter(e -> e.getKey().isPresent())
            .collect(Collectors.toMap(e -> e.getKey().get(), Map.Entry::getValue));
    }

    private String getLinksReply(Map<LinkType, List<URI>> links) {
        return links.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().ordinal()))
            .map(e -> getLinkTypeResponse(e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n\n"));
    }

    private String getLinkTypeResponse(LinkType linkType, List<URI> links) {
        return String.format("%s *%s*\n", Emoji.LINK.getMarkdown(), linkType)
            + links.stream()
            .map(link -> String.format("➜ %s", link))
            .collect(Collectors.joining("\n"));
    }
}
