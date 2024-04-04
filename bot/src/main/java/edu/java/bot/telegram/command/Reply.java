package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.util.TextUtil;

public interface Reply {

    default boolean isReply(Update update, String expectedReply) {
        Message reply = update.message().replyToMessage();
        return reply != null
            && TextUtil.clearBotReplyMessage(expectedReply).equals(TextUtil.clearTgMessage(reply.text()));
    }

    SendMessage handleReply(Update update);
}
