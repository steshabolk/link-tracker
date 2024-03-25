package edu.java.service.jdbc;

import edu.java.entity.Chat;
import edu.java.exception.ApiExceptionType;
import edu.java.repository.jdbc.JdbcChatRepository;
import edu.java.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JdbcChatService implements ChatService {

    private final JdbcChatRepository chatRepository;

    @Override
    public Chat findByChatId(Long chatId) {
        return chatRepository.findByChatId(chatId)
            .orElseThrow(() -> ApiExceptionType.CHAT_NOT_FOUND.toException(chatId));
    }

    @Transactional
    @Override
    public void registerChat(Long chatId) {
        if (chatRepository.existsByChatId(chatId)) {
            throw ApiExceptionType.CHAT_ALREADY_EXISTS.toException(chatId);
        }
        Chat chat = Chat.builder().chatId(chatId).build();
        chatRepository.save(chat);
        log.debug("new chat{chatId={}} was registered", chatId);
    }

    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        if (!chatRepository.existsByChatId(chatId)) {
            throw ApiExceptionType.CHAT_NOT_FOUND.toException(chatId);
        }
        chatRepository.delete(chatId);
        log.debug("chat{chatId={}} was deleted", chatId);
    }
}
