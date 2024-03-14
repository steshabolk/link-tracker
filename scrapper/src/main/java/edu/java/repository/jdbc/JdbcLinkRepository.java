package edu.java.repository.jdbc;

import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.repository.LinkRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class JdbcLinkRepository implements LinkRepository {

    private static final String SAVE_LINK = """
        INSERT INTO links(link_type, url, checked_at, status)
        VALUES (?, ?, ?, ?)
        RETURNING id, link_type, url, checked_at, status
        """;
    private static final String FIND_LINK_BY_URL = """
        SELECT id, link_type, url, checked_at, status FROM links WHERE url = ?
        """;
    private static final String FIND_LINKS_BY_CHAT_ID = """
        SELECT l.id, l.link_type, l.url, l.checked_at, l.status
        FROM links l
        JOIN chats_links cl ON l.id = cl.link_id
        WHERE cl.chat_id = ?
        """;
    private static final String FIND_LINKS_OLDER_THAN = """
        SELECT l.id, l.link_type, l.url, l.checked_at, l.status,
            c.id AS c_id, c.chat_id AS chat_id
        FROM links l
        JOIN chats_links cl ON l.id = cl.link_id
        JOIN chats c ON cl.chat_id = c.id
        WHERE l.status = ? AND l.checked_at < ?
        LIMIT ?
        """;
    private static final String UPDATE_LINK_STATUS = """
        UPDATE links SET status = ? WHERE id = ?
        """;
    private static final String UPDATE_CHECKED_AT = """
        UPDATE links SET checked_at = ? WHERE id = ?
        """;
    private static final RowMapper<Link> LINK_ROW_MAPPER = (rs, rowNum) -> mapLinkFromResultSet(rs);
    private static final ResultSetExtractor<List<Link>> LINKS_WITH_CHATS_RESULT_SET_EXTRACTOR = rs -> {
        Map<Long, Link> linkMap = new HashMap<>();
        while (rs.next()) {
            Link link = linkMap.get(rs.getLong("id"));
            if (link == null) {
                link = mapLinkFromResultSet(rs);
                linkMap.put(link.getId(), link);
            }
            Chat chat = mapChatFromResultSet(rs);
            link.getChats().add(chat);
        }
        return new ArrayList<>(linkMap.values());
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Link save(Link link) {
        return jdbcTemplate.queryForObject(SAVE_LINK, LINK_ROW_MAPPER,
            link.getLinkType().ordinal(),
            link.getUrl(),
            link.getCheckedAt(),
            link.getStatus().ordinal()
        );
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return jdbcTemplate.query(FIND_LINKS_BY_CHAT_ID, LINK_ROW_MAPPER, chat.getId());
    }

    @Override
    public Link findByUrl(String url) {
        List<Link> links = jdbcTemplate.query(FIND_LINK_BY_URL, LINK_ROW_MAPPER, url);
        return links.stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean updateStatus(Link link, LinkStatus status) {
        return jdbcTemplate.update(UPDATE_LINK_STATUS, status.ordinal(), link.getId()) > 0;
    }

    @Override
    public boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        return jdbcTemplate.update(UPDATE_CHECKED_AT, checkedAt, link.getId()) > 0;
    }

    @Override
    public List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Integer limit) {
        return jdbcTemplate.query(FIND_LINKS_OLDER_THAN,
            LINKS_WITH_CHATS_RESULT_SET_EXTRACTOR, status.ordinal(), checkedAt, limit
        );
    }

    private static Link mapLinkFromResultSet(ResultSet rs) throws SQLException {
        return Link.builder()
            .id(rs.getLong("id"))
            .linkType(LinkType.values()[rs.getInt("link_type")])
            .url(rs.getString("url"))
            .checkedAt(rs.getObject("checked_at", OffsetDateTime.class))
            .status(LinkStatus.values()[rs.getInt("status")])
            .build();
    }

    private static Chat mapChatFromResultSet(ResultSet rs) throws SQLException {
        return Chat.builder()
            .id(rs.getLong("c_id"))
            .chatId(rs.getLong("chat_id"))
            .build();
    }
}
