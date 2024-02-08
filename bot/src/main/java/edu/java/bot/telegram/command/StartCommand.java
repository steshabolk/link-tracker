package edu.java.bot.telegram.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.enums.CommandType;
import edu.java.bot.enums.Emoji;
import edu.java.bot.sender.BotSender;
import edu.java.bot.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {

    private final CommandType commandType = CommandType.START;
    private final BotSender sender;
    private final ScrapperService scrapperService;
    private final String reply = String.format(
        "*hi!* %s\nthis is a link tracking bot %s\n%s",
        Emoji.WAVE.getMarkdown(),
        Emoji.ROBOT.getMarkdown(),
        CommandType.HELP.getCommandBulletPoint()
    );

    @Autowired
    public StartCommand(BotSender sender, ScrapperService scrapperService) {
        this.sender = sender;
        this.scrapperService = scrapperService;
    }

    @Override
    public CommandType commandType() {
        return commandType;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        scrapperService.register(chatId, update.message().from().id());
        return sender.getSendMessage(chatId, reply);
    }
}
