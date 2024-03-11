package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMemberUpdated;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.BotReply;
import edu.java.bot.service.ScrapperService;
import edu.java.bot.telegram.command.Command;
import edu.java.bot.util.BotSendMessage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageHandler {

    private final List<Command> commands;
    private final ScrapperService scrapperService;
    private final ClientExceptionHandler clientExceptionHandler;

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
            return clientExceptionHandler.getReplyForScrapperErrorResponse(ex, chatId);
        }
    }

    private void handleMemberStatus(Update update) {
        getChatMember(update)
            .ifPresentOrElse(
                it -> handleNewMemberStatus(it.chat().id(), it.newChatMember().status()),
                () -> log.warn("error processing update: {}", update)
            );
    }

    private Optional<ChatMemberUpdated> getChatMember(Update update) {
        return Optional.ofNullable(update.myChatMember())
            .filter(it -> it.newChatMember() != null && it.newChatMember().status() != null)
            .filter(it -> it.chat() != null && it.chat().id() != null);
    }

    private void handleNewMemberStatus(Long chatId, ChatMember.Status newStatus) {
        switch (newStatus) {
            case ChatMember.Status.kicked -> {
                log.debug("chat={}: bot was blocked", chatId);
                scrapperService.deleteChat(chatId);
            }
            case ChatMember.Status.member -> log.debug("chat={}: bot was started", chatId);
            default -> log.debug("chat={}: new member status={}", chatId, newStatus.name());
        }
    }
}
