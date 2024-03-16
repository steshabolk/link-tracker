package edu.java.repository.jooq;

import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.model.jooq.tables.records.LinksRecord;
import edu.java.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Record6;
import org.jooq.Result;
import static edu.java.model.jooq.Tables.CHATS;
import static edu.java.model.jooq.Tables.CHATS_LINKS;
import static edu.java.model.jooq.Tables.LINKS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@RequiredArgsConstructor
public class JooqLinkRepository implements LinkRepository {

    private final DSLContext context;

    @Override
    public Link save(Link link) {
        return context
            .insertInto(LINKS)
            .set(LINKS.LINK_TYPE, (short) link.getLinkType().ordinal())
            .set(LINKS.URL, link.getUrl())
            .set(LINKS.CHECKED_AT, link.getCheckedAt())
            .set(LINKS.STATUS, (short) link.getStatus().ordinal())
            .returning(LINKS.fields())
            .fetchOne(this::mapLinkFromRecord);
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return context
            .select(LINKS.fields())
            .from(LINKS)
            .join(CHATS_LINKS)
            .on(LINKS.ID.eq(CHATS_LINKS.LINK_ID))
            .where(CHATS_LINKS.CHAT_ID.eq(chat.getId()))
            .fetchInto(LINKS)
            .map(this::mapLinkFromRecord);
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return context
            .selectFrom(LINKS)
            .where(LINKS.URL.eq(url))
            .fetchOptional(this::mapLinkFromRecord);
    }

    @Override
    public boolean updateStatus(Link link, LinkStatus status) {
        return context
            .update(LINKS)
            .set(LINKS.STATUS, (short) status.ordinal())
            .where(LINKS.ID.eq(link.getId()))
            .execute() > 0;
    }

    @Override
    public boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        return context
            .update(LINKS)
            .set(LINKS.CHECKED_AT, checkedAt)
            .where(LINKS.ID.eq(link.getId()))
            .execute() > 0;
    }

    @Override
    public List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Integer limit) {
        return context
            .select(
                LINKS.ID,
                LINKS.LINK_TYPE,
                LINKS.URL,
                LINKS.CHECKED_AT,
                LINKS.STATUS,
                multiset(
                    select(CHATS.ID, CHATS.CHAT_ID)
                        .from(CHATS)
                        .join(CHATS_LINKS)
                        .on(CHATS_LINKS.CHAT_ID.eq(CHATS.ID))
                        .where(CHATS_LINKS.LINK_ID.eq(LINKS.ID))
                ).as("chats").convertFrom(this::mapChatsFromRecord)
            )
            .from(LINKS)
            .where(LINKS.STATUS.eq((short) status.ordinal())
                .and(LINKS.CHECKED_AT.lt(checkedAt)))
            .limit(limit)
            .fetch()
            .map(this::mapLinksWithChatsFromRecord);
    }

    private Link mapLinkFromRecord(LinksRecord r) {
        return buildLink(r.getId(), r.getLinkType(), r.getUrl(), r.getCheckedAt(), r.getStatus(), new HashSet<>());
    }

    private Link mapLinksWithChatsFromRecord(Record6<Long, Short, String, OffsetDateTime, Short, List<Chat>> r) {
        return buildLink(r.value1(), r.value2(), r.value3(), r.value4(), r.value5(), new HashSet<>(r.value6()));
    }

    private Link buildLink(
        Long id,
        Short linkType,
        String url,
        OffsetDateTime checkedAt,
        Short status,
        Set<Chat> chats
    ) {
        return Link.builder()
            .id(id)
            .linkType(LinkType.values()[linkType])
            .url(url)
            .checkedAt(checkedAt)
            .status(LinkStatus.values()[status])
            .chats(chats)
            .build();
    }

    private List<Chat> mapChatsFromRecord(Result<Record2<Long, Long>> res) {
        return res.map(r -> Chat.builder()
            .id(r.value1())
            .chatId(r.value2())
            .build());
    }
}
