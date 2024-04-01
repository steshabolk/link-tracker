package edu.java.repository.jdbc.mapper;

import edu.java.entity.Chat;
import edu.java.entity.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LinkWithChatsResultSetExtractor implements ResultSetExtractor<List<Link>> {

    private final LinkMapper linkMapper;

    @Override
    public List<Link> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Link> linkMap = new HashMap<>();
        while (rs.next()) {
            Link link = linkMap.get(rs.getLong("id"));
            if (link == null) {
                link = linkMapper.mapRow(rs, 0);
                linkMap.put(link.getId(), link);
            }
            Chat chat = mapChatFromResultSet(rs);
            link.getChats().add(chat);
        }
        return new ArrayList<>(linkMap.values());
    }

    private static Chat mapChatFromResultSet(ResultSet rs) throws SQLException {
        return Chat.builder()
            .id(rs.getLong("c_id"))
            .chatId(rs.getLong("chat_id"))
            .build();
    }
}
