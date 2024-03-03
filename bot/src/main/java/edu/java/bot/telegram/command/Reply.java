package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;

public interface Reply {

    default boolean isReply(Update update, String botReply) {
        Message reply = update.message().replyToMessage();
        return reply != null && EmojiParser.removeAllEmojis(EmojiParser.parseToUnicode(botReply)).replace("*", "")
            .equals(EmojiParser.removeAllEmojis(reply.text()));
    }

    SendMessage handleReply(Update update);
}
