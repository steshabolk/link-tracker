package edu.java.entity;

import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(exclude = {"chats"})
@Getter
@Setter
@Builder
public class Link {

    private Long id;

    private LinkType linkType;

    private String url;

    @Builder.Default
    private OffsetDateTime checkedAt = OffsetDateTime.now();

    @Builder.Default
    private LinkStatus status = LinkStatus.ACTIVE;

    @Builder.Default
    private Set<Chat> chats = new HashSet<>();
}
