package edu.java.bot.util;

import com.vdurmont.emoji.EmojiParser;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtil {

    public static String toBold(String text) {
        return String.format("%s%s%s", "<b>", text, "</b>");
    }

    public static String clearBotReplyMessage(String text) {
        return text
            .transform(EmojiParser::removeAllEmojis)
            .transform(TextUtil::removeHtmlSyntax);
    }

    public static String clearTgMessage(String text) {
        return text.transform(EmojiParser::removeAllEmojis);
    }

    public static String removeHtmlSyntax(String text) {
        return text.replaceAll("<(/)?b>", "");
    }

    public static String escapeSpecialCharacters(String text) {
        return text
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
    }
}
