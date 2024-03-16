package edu.java.repository.jooq;

import edu.java.entity.Chat;
import edu.java.repository.ChatRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import static edu.java.model.jooq.Tables.CHATS;

@RequiredArgsConstructor
public class JooqChatRepository implements ChatRepository {

    private final DSLContext context;

    @Override
    public Chat save(Chat chat) {
        return context
            .insertInto(CHATS)
            .set(CHATS.CHAT_ID, chat.getChatId())
            .returning(CHATS.ID, CHATS.CHAT_ID)
            .fetchOneInto(Chat.class);
    }

    @Override
    public boolean delete(Long chatId) {
        return context
            .deleteFrom(CHATS)
            .where(CHATS.CHAT_ID.eq(chatId))
            .execute() > 0;
    }

    @Override
    public Optional<Chat> findByChatId(Long chatId) {
        return context
            .select(CHATS.ID, CHATS.CHAT_ID)
            .from(CHATS)
            .where(CHATS.CHAT_ID.eq(chatId))
            .fetchOptionalInto(Chat.class);
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return context
            .fetchExists(CHATS, CHATS.CHAT_ID.eq(chatId));
    }
}
