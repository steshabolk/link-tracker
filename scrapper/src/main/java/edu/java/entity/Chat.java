package edu.java.entity;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public class Chat {

    private Long id;

    private Long chatId;

    private Long userId;

    private Set<Link> links = new HashSet<>();
}
