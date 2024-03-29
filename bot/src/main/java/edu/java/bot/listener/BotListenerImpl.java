package edu.java.bot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import edu.java.bot.enums.CommandType;
import edu.java.bot.handler.MessageHandler;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotListenerImpl implements BotListener {

    private final TelegramBot bot;
    private final SetMyCommands menu;
    private final MessageHandler messageHandler;

    @Autowired
    public BotListenerImpl(TelegramBot bot, MessageHandler messageHandler) {
        this.bot = bot;
        this.messageHandler = messageHandler;
        menu = new SetMyCommands(Arrays.stream(CommandType.values())
            .map(cmd -> new BotCommand(cmd.getCommand(), cmd.getDescription()))
            .toArray(BotCommand[]::new));
    }

    @PostConstruct
    @Override
    public void start() {
        telegramBot().setUpdatesListener(
            this,
            ex -> {
                if (ex.response() != null) {
                    log.error(ex.response().errorCode() + ": " + ex.response().description());
                } else {
                    log.error(ex.getMessage());
                }
            }
        );
        execute(menu);
    }

    @Override
    public TelegramBot telegramBot() {
        return bot;
    }

    @Override
    public int process(List<Update> list) {
        list.forEach(this::processUpdate);
        return CONFIRMED_UPDATES_ALL;
    }

    @Override
    public void processUpdate(Update update) {
        SendMessage message = messageHandler.handle(update);
        if (message != null) {
            bot.execute(message);
        }
    }
}
