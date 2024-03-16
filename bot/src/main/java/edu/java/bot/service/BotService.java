package edu.java.bot.service;

import com.pengrad.telegrambot.response.SendResponse;
import edu.java.bot.dto.request.LinkUpdateRequest;
import edu.java.bot.enums.Emoji;
import edu.java.bot.listener.BotListenerImpl;
import edu.java.bot.util.BotSendMessage;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BotService {

    private final BotListenerImpl botListener;
    private final ScrapperService scrapperService;

    public void sendLinkUpdate(LinkUpdateRequest linkUpdate) {
        log.debug(
            "send link update to the bot: id={}\nurl={}\nmessage={}",
            linkUpdate.id(),
            linkUpdate.url(),
            linkUpdate.description()
        );
        String updateResponse = getUpdateResponse(linkUpdate.url(), linkUpdate.description());
        linkUpdate.tgChatIds()
            .forEach(chatId -> sendMessageToBot(chatId, updateResponse));
    }

    private void sendMessageToBot(Long chatId, String updateResponse) {
        SendResponse botResponse = botListener.execute(BotSendMessage.getSendMessage(chatId, updateResponse));
        if (!botResponse.isOk()) {
            log.debug("chat={}: {} {}", chatId, botResponse.errorCode(), botResponse.description());
            if (botResponse.errorCode() == HttpStatus.FORBIDDEN.value()) {
                scrapperService.deleteChat(chatId);
            }
        }
    }

    private String getUpdateResponse(URI url, String message) {
        return String.format("%s %s\n%s", Emoji.LINK.getMarkdown(), url, message)
            .replace("[", "\\[");
    }
}
