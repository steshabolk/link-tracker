package edu.java.bot.enums;

public enum LinkType {
    GITHUB("github.com"),
    STACKOVERFLOW("stackoverflow.com");
    private final String domain;

    LinkType(String domain) {
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public String getCommandBulletPoint() {
        return String.format("◉ %s", this.getDomain());
    }
}
