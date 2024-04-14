package edu.java.bot.enums;

import edu.java.bot.util.TextUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandType {

    START("/start", "start the bot"),
    TRACK("/track", "start tracking a link"),
    UNTRACK("/untrack", "stop tracking a link"),
    LIST("/list", "show a list of tracked links"),
    HELP("/help", "show commands");

    private final String command;
    private final String description;

    public String getCommandBulletPoint() {
        return String.format("➜ %s - %s", TextUtil.toBold(this.getCommand()), this.getDescription());
    }
}
