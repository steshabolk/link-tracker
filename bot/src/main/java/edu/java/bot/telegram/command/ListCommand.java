package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.util.BotSendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ListCommand implements Command {

    private final CommandType commandType = CommandType.LIST;
    private static final List<LinkType> DOMAINS = Arrays.stream(LinkType.values()).toList();
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
        String linksReply = links.stream()
            .collect(Collectors.groupingBy(
                link -> DOMAINS.stream()
                    .filter(it -> it.getDomain().equals(link.getHost()))
                    .findFirst()
            ))
            .entrySet().stream()
            .filter(e -> e.getKey().isPresent())
            .sorted(Comparator.comparingInt(e -> DOMAINS.indexOf(e.getKey().get())))
            .map(e -> String.format("%s *%s*\n", Emoji.LINK.getMarkdown(), e.getKey().get())
                + e.getValue().stream()
                .map(link -> String.format("➜ %s", link))
                .collect(Collectors.joining("\n")))
            .collect(Collectors.joining("\n\n"));
        return BotSendMessage.getSendMessage(chatId, linksReply);
    }
}
