package edu.java.bot.enums;

public enum Emoji {

    WAVE(":wave:"),
    ROBOT(":robot_face:"),
    BOOKMARK(":bookmark_tabs:"),
    LINK(":link:"),
    INFO(":information_source:"),
    ERROR(":heavy_multiplication_x:"),
    CHECK(":heavy_check_mark:");

    private final String markdown;

    Emoji(String markdown) {
        this.markdown = markdown;
    }

    public String getMarkdown() {
        return markdown;
    }
}
