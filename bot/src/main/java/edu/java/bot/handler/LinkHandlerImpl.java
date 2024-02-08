package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.dto.LinkDto;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import edu.java.bot.exception.ApiException;
import edu.java.bot.sender.BotSender;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkHandlerImpl implements LinkHandler {

    private final String urlPattern = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";
    private final String invalidLinkReply =
        String.format("%s your link is invalid. please try again", Emoji.ERROR.getMarkdown());
    private final String unsupportedLinkReply =
        String.format("%s sorry, tracking is not supported on this resource", Emoji.ERROR.getMarkdown());
    private final List<LinkType> domains = Arrays.stream(LinkType.values()).toList();
    private final BotSender sender;

    @Autowired
    public LinkHandlerImpl(BotSender sender) {
        this.sender = sender;
    }

    public SendMessage handleLink(Update update, BiConsumer<Long, LinkDto> linkOperation, String successReply) {
        Long chatId = update.message().chat().id();
        String message = update.message().text().trim();
        LinkDto linkDto;
        try {
            linkDto = parseLink(message);
        } catch (ApiException ex) {
            log.debug(String.format("error processing link=%s", message));
            return sender.getSendMessage(chatId, ex.getMessage());
        }
        linkOperation.accept(chatId, linkDto);
        return sender.getSendMessage(chatId, successReply);
    }

    @Override
    public LinkDto parseLink(String url) {
        if (!Pattern.matches(urlPattern, url)) {
            throw new ApiException(invalidLinkReply);
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (RuntimeException ex) {
            throw new ApiException(invalidLinkReply);
        }
        List<LinkType> linkType = domains.stream().filter(domain -> domain.getDomain().equals(uri.getHost())).toList();
        if (linkType.size() != 1) {
            throw new ApiException(unsupportedLinkReply);
        }
        return new LinkDto(linkType.get(0), uri);
    }
}
