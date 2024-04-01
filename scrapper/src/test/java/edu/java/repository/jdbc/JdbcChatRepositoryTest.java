package edu.java.repository.jdbc;

import edu.java.entity.Chat;
import edu.java.integration.IntegrationTest;
import edu.java.integration.config.JdbcTestConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JdbcChatRepository.class})
@ContextConfiguration(classes = {JdbcTestConfig.class})
class JdbcChatRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcChatRepository chatRepository;

    private static final Chat CHAT = Chat.builder().chatId(123L).build();

    @Nested
    class SaveChatTest {

        @Test
        @Transactional
        @Rollback
        void shouldReturnSavedChat() {
            Chat saved = chatRepository.save(CHAT);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getChatId()).isEqualTo(123);
        }
    }

    @Nested
    class DeleteChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenChatWasDeleted() {
            boolean isRemoved = chatRepository.delete(CHAT.getChatId());

            assertTrue(isRemoved);
        }
    }

    @Nested
    class FindByChatIdTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnChat() {
            Optional<Chat> chat = chatRepository.findByChatId(CHAT.getChatId());

            assertThat(chat).isPresent();
            assertThat(chat.get().getId()).isEqualTo(1);
            assertThat(chat.get().getChatId()).isEqualTo(123);
        }
    }

    @Nested
    class ExistsByChatIdTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenChatExists() {
            boolean exists = chatRepository.existsByChatId(CHAT.getChatId());

            assertTrue(exists);
        }

        @Test
        @Transactional
        void shouldReturnFalseWhenChatDoesNotExists() {
            boolean exists = chatRepository.existsByChatId(CHAT.getChatId());

            assertFalse(exists);
        }
    }
}
