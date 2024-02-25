package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;

public interface Command {

    CommandType commandType();

    default String command() {
        return commandType().getCommand();
    }

    default String description() {
        return commandType().getDescription();
    }

    default boolean isTriggered(Update update) {
        return update.message().text().trim().equals(command());
    }

    SendMessage handle(Update update);
}
