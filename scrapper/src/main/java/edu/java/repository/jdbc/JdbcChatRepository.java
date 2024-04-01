package edu.java.repository.jdbc;

import edu.java.entity.Chat;
import edu.java.repository.ChatRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
public class JdbcChatRepository implements ChatRepository {

    private static final String SAVE_CHAT = """
        INSERT INTO chats(chat_id) VALUES (?) RETURNING id, chat_id
        """;
    private static final String DELETE_CHAT_BY_CHAT_ID = """
        DELETE FROM chats WHERE chat_id = ?
        """;
    private static final String FIND_CHAT_BY_CHAT_ID = """
        SELECT id, chat_id FROM chats WHERE chat_id = ?
        """;
    private static final String EXISTS_BY_CHAT_ID = """
        SELECT count(id) FROM chats WHERE chat_id = ?
        """;
    private static final RowMapper<Chat> CHAT_ROW_MAPPER = (rs, rowNum) ->
        Chat.builder()
            .id(rs.getLong("id"))
            .chatId(rs.getLong("chat_id"))
            .build();

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Chat save(Chat chat) {
        return jdbcTemplate.queryForObject(SAVE_CHAT, CHAT_ROW_MAPPER, chat.getChatId());
    }

    @Override
    public boolean delete(Long chatId) {
        return jdbcTemplate.update(DELETE_CHAT_BY_CHAT_ID, chatId) > 0;
    }

    @Override
    public Optional<Chat> findByChatId(Long chatId) {
        return jdbcTemplate.queryForStream(FIND_CHAT_BY_CHAT_ID, CHAT_ROW_MAPPER, chatId).findFirst();
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(EXISTS_BY_CHAT_ID, Integer.class, chatId))
            .map(it -> it > 0)
            .orElse(false);
    }
}
