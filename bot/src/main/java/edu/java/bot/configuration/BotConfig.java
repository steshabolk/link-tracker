package edu.java.bot.configuration;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    private final ApplicationConfig applicationConfig;

    @Autowired
    public BotConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(applicationConfig.telegramToken());
    }
}
