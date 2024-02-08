package edu.java.bot.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.telegram.command.Command;
import java.util.List;

public interface MessageHandler {

    List<Command> commands();

    SendMessage handle(Update update);
}
