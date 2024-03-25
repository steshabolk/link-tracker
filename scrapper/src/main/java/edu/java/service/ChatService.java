package edu.java.service;

import edu.java.entity.Chat;

public interface ChatService {

    Chat findByChatId(Long chatId);

    void registerChat(Long chatId);

    void deleteChat(Long chatId);
}
