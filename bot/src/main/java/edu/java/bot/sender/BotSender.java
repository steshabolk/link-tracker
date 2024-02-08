package edu.java.bot.sender;

import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;

@Component
public class BotSender {

    public SendMessage getSendMessage(Long chatId, String text) {
        return getSendMessage(chatId, text, false);
    }

    public SendMessage getSendMessage(Long chatId, String text, boolean withReply) {
        SendMessage message = new SendMessage(chatId, EmojiParser.parseToUnicode(text))
            .parseMode(ParseMode.Markdown);
        if (withReply) {
            message.replyMarkup(new ForceReply());
        }
        return message;
    }
}
