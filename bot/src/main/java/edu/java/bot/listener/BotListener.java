package edu.java.bot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.List;

public interface BotListener extends UpdatesListener {

    TelegramBot telegramBot();

    default <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        return telegramBot().execute(request);
    }

    @Override
    int process(List<Update> list);

    void processUpdate(Update update);

    void start();

    default void close() {
        telegramBot().shutdown();
    }
}
