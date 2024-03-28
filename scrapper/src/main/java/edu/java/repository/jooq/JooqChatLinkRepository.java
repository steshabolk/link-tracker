package edu.java.repository.jooq;

import edu.java.configuration.DatabaseAccessConfig;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.repository.ChatLinkRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import static edu.java.model.jooq.Tables.CHATS_LINKS;

@RequiredArgsConstructor
@ConditionalOnBean(DatabaseAccessConfig.JooqAccessConfig.class)
@Repository
public class JooqChatLinkRepository implements ChatLinkRepository {

    private final DSLContext context;

    @Override
    public boolean isLinkAddedToChat(Chat chat, Link link) {
        return context
            .fetchExists(
                CHATS_LINKS,
                CHATS_LINKS.CHAT_ID.eq(chat.getId())
                    .and(CHATS_LINKS.LINK_ID.eq(link.getId()))
            );
    }

    @Override
    public boolean addLinkToChat(Chat chat, Link link) {
        return context
            .insertInto(CHATS_LINKS)
            .set(CHATS_LINKS.CHAT_ID, chat.getId())
            .set(CHATS_LINKS.LINK_ID, link.getId())
            .execute() > 0;
    }

    @Override
    public boolean removeLinkFromChat(Chat chat, Link link) {
        return context
            .deleteFrom(CHATS_LINKS)
            .where(CHATS_LINKS.CHAT_ID.eq(chat.getId())
                .and(CHATS_LINKS.LINK_ID.eq(link.getId())))
            .execute() > 0;
    }
}
