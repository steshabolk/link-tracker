package edu.java.bot.enums;

public enum CommandType {

    START("/start", "start the bot"),
    HELP("/help", "show commands"),
    TRACK("/track", "start tracking a link"),
    UNTRACK("/untrack", "stop tracking a link"),
    LIST("/list", "show a list of tracked links");

    private final String command;
    private final String description;

    CommandType(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getCommandBulletPoint() {
        return String.format("◉ *%s* ➜ %s", this.getCommand(), this.getDescription());
    }
}
