package edu.java.repository.jdbc.mapper;

import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Link.builder()
            .id(rs.getLong("id"))
            .linkType(LinkType.values()[rs.getInt("link_type")])
            .url(rs.getString("url"))
            .checkedAt(rs.getObject("checked_at", OffsetDateTime.class))
            .status(LinkStatus.values()[rs.getInt("status")])
            .build();
    }
}
