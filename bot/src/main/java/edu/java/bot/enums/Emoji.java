package edu.java.bot.enums;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Emoji {

    WAVE(":wave:"),
    ROBOT(":robot_face:"),
    BOOKMARK(":bookmark_tabs:"),
    LINK(":link:"),
    INFO(":information_source:"),
    ERROR(":heavy_multiplication_x:"),
    CHECK(":heavy_check_mark:");

    private final String markdown;

    public String toUnicode() {
        return EmojiParser.parseToUnicode(this.markdown);
    }
}
