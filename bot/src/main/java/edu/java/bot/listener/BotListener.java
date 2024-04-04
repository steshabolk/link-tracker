package edu.java.bot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.enums.CommandType;
import edu.java.bot.handler.MessageHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotListener implements UpdatesListener {

    private final TelegramBot bot;
    private final SetMyCommands menu;
    private final MessageHandler messageHandler;

    @Autowired
    public BotListener(TelegramBot bot, MessageHandler messageHandler) {
        this.bot = bot;
        this.messageHandler = messageHandler;
        menu = new SetMyCommands(Arrays.stream(CommandType.values())
            .map(cmd -> new BotCommand(cmd.getCommand(), cmd.getDescription()))
            .toArray(BotCommand[]::new));
    }

    @PostConstruct
    private void start() {
        bot.setUpdatesListener(
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
    public int process(List<Update> list) {
        list.forEach(this::processUpdate);
        return CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        SendMessage message = messageHandler.handle(update);
        if (message != null) {
            bot.execute(message);
        }
    }

    public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        return bot.execute(request);
    }

    @PreDestroy
    private void close() {
        bot.shutdown();
    }
}
