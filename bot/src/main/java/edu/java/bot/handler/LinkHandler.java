package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.util.BotSendMessage;
import edu.java.bot.util.LinkParser;
import java.net.URI;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkHandler {

    public SendMessage handleLink(Update update, BiConsumer<Long, URI> linkOperation, String operationReply) {
        Long chatId = update.message().chat().id();
        String message = update.message().text().trim();
        URI link;
        try {
            link = LinkParser.parseLink(message);
        } catch (RuntimeException ex) {
            log.debug("error processing link={}", message);
            return BotSendMessage.getSendMessage(chatId, ex.getMessage());
        }
        linkOperation.accept(chatId, link);
        return BotSendMessage.getSendMessage(chatId, operationReply);
    }
}
