package edu.java.bot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMemberUpdated;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.dto.response.ScrapperErrorResponse;
import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.ScrapperExceptionCode;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.telegram.command.Command;
import edu.java.bot.util.BotSendMessage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageHandler {

    private final List<Command> commands;
    private final ScrapperService scrapperService;
    private final ObjectMapper mapper = new ObjectMapper();

    public SendMessage handle(Update update) {
        Long chatId = Optional.ofNullable(update.message())
            .filter(message -> StringUtils.hasText(message.text()))
            .map(Message::chat)
            .map(Chat::id)
            .orElse(null);
        if (chatId == null) {
            handleMemberStatus(update);
            return null;
        }
        Optional<Command> command = commands.stream()
            .filter(cmd -> cmd.isTriggered(update))
            .findFirst();
        if (command.isEmpty()) {
            return BotSendMessage.getSendMessage(chatId, BotReply.UNKNOWN_COMMAND.getReply());
        }
        try {
            return command.get().handle(update);
        } catch (RuntimeException ex) {
            return handleCommandException(ex, chatId);
        }
    }

    private void handleMemberStatus(Update update) {
        Optional.ofNullable(update.myChatMember())
            .filter(it -> it.newChatMember() != null && it.newChatMember().status() != null)
            .map(ChatMemberUpdated::chat)
            .map(Chat::id)
            .ifPresentOrElse(
                chatId -> {
                    ChatMember.Status newStatus = update.myChatMember().newChatMember().status();
                    switch (newStatus) {
                        case ChatMember.Status.kicked -> {
                            log.debug("chat={}: bot was blocked", chatId);
                            scrapperService.deleteChat(chatId);
                        }
                        case ChatMember.Status.member -> log.debug("chat={}: bot was started", chatId);
                        default -> log.debug("chat={}: new member status={}", chatId, newStatus.name());
                    }
                },
                () -> log.warn("error processing update: {}", update)
            );
    }

    private SendMessage handleCommandException(RuntimeException ex, Long chatId) {
        log.info("error processing command: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc
            && ArrayUtils.isNotEmpty(clientExc.getResponseBodyAsByteArray())) {
            log.info("client response: {}", clientExc.getResponseBodyAsString());
            ScrapperErrorResponse response = parseScrapperResponseBody(clientExc.getResponseBodyAsByteArray());
            if (response != null) {
                return getReplyForScrapperError(chatId, response);
            }
        }
        return null;
    }

    private ScrapperErrorResponse parseScrapperResponseBody(byte[] responseBody) {
        ScrapperErrorResponse response;
        try {
            response = mapper.readValue(responseBody, ScrapperErrorResponse.class);
        } catch (Exception ex) {
            response = null;
        }
        return response;
    }

    private SendMessage getReplyForScrapperError(Long chatId, ScrapperErrorResponse errorResponse) {
        ScrapperExceptionCode code;
        try {
            code = ScrapperExceptionCode.valueOf(errorResponse.code());
        } catch (RuntimeException ex) {
            log.info("cant parse scrapper exception code: {}", errorResponse.code());
            return null;
        }
        String reply = switch (code) {
            case CHAT_NOT_FOUND -> BotReply.CHAT_NOT_FOUND.getReply();
            case LINK_NOT_FOUND -> BotReply.LINK_NOT_FOUND.getReply();
            case LINK_ALREADY_EXISTS -> BotReply.LINK_ALREADY_EXISTS.getReply();
            case INVALID_LINK -> BotReply.INVALID_LINK.getReply();
            case NOT_SUPPORTED_SOURCE -> BotReply.NOT_SUPPORTED_LINK.getReply();
            default -> null;
        };
        return Optional.ofNullable(reply)
            .map(it -> BotSendMessage.getSendMessage(chatId, it))
            .orElse(null);
    }
}
