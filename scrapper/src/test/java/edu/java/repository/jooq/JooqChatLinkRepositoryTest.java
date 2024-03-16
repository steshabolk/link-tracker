package edu.java.repository.jooq;

import edu.java.configuration.JooqConfig;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.integration.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JooqTest
@ContextConfiguration(classes = {JooqChatLinkRepository.class, JooqConfig.class})
class JooqChatLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JooqChatLinkRepository chatLinkRepository;

    private static final Chat CHAT = Chat.builder().id(1L).build();
    private static final Link LINK = Link.builder().id(1L).build();

    @Nested
    class IsLinkAddedToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql", "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkIsAddedToChat() {
            boolean isLinkAddedToChat = chatLinkRepository.isLinkAddedToChat(CHAT, LINK);

            assertTrue(isLinkAddedToChat);
        }

        @Test
        @Transactional
        void shouldReturnFalseWhenLinkIsNotAddedToChat() {
            boolean isLinkAddedToChat = chatLinkRepository.isLinkAddedToChat(CHAT, LINK);

            assertFalse(isLinkAddedToChat);
        }
    }

    @Nested
    class AddLinkToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkWasAdded() {
            boolean isAdded = chatLinkRepository.addLinkToChat(CHAT, LINK);

            assertTrue(isAdded);
        }
    }

    @Nested
    class RemoveLinkFromChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql", "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenLinkWasRemoved() {
            boolean isRemoved = chatLinkRepository.removeLinkFromChat(CHAT, LINK);

            assertTrue(isRemoved);
        }
    }
}
