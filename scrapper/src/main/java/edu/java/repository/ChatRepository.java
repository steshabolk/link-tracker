package edu.java.repository;

import edu.java.entity.Chat;

public interface ChatRepository {

    Chat save(Chat chat);

    boolean delete(Long chatId);

    Chat findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);
}
