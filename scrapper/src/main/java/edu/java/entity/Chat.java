package edu.java.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(exclude = {"links"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Chat {

    private Long id;

    private Long chatId;

    @Builder.Default
    private Set<Link> links = new HashSet<>();
}
