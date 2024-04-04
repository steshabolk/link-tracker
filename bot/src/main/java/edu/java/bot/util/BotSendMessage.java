package edu.java.bot.util;

import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BotSendMessage {

    public static SendMessage getSendMessage(Long chatId, String text) {
        return getSendMessage(chatId, text, false);
    }

    public static SendMessage getSendMessage(Long chatId, String text, boolean withReply) {
        SendMessage message = new SendMessage(chatId, text)
            .parseMode(ParseMode.HTML);
        if (withReply) {
            message.replyMarkup(new ForceReply());
        }
        return message;
    }
}
