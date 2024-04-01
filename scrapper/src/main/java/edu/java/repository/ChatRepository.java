package edu.java.repository;

import edu.java.entity.Chat;
import java.util.Optional;

public interface ChatRepository {

    Chat save(Chat chat);

    boolean delete(Long chatId);

    Optional<Chat> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);
}
