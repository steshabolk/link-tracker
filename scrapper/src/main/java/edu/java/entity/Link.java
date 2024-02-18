package edu.java.entity;

import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Link {

    private Long id;

    private LinkType linkType;

    private String url;

    private OffsetDateTime checkedAt;

    private LinkStatus status;

    @Builder.Default
    private Set<Chat> chats = new HashSet<>();
}
