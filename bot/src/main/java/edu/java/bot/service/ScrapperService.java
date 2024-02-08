package edu.java.bot.service;

import edu.java.bot.dto.LinkDto;
import edu.java.bot.enums.LinkType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScrapperService {

    private final Map<LinkType, List<URI>> links;

    public ScrapperService() {
        this.links = new HashMap<>();
        Arrays.stream(LinkType.values()).forEach(link -> links.put(link, new ArrayList<>()));
    }

    public void register(Long chatId, Long userId) {
        log.debug(String.format("the user{id=%s} has registered the chat{id=%s}", userId, chatId));
    }

    public void track(Long chatId, LinkDto linkDto) {
        log.debug(String.format("chat{id=%s}: track the link=%s", chatId, linkDto.uri()));
        links.get(linkDto.linkType()).add(linkDto.uri());
    }

    public void untrack(Long chatId, LinkDto linkDto) {
        log.debug(String.format("chat{id=%s}: untrack the link=%s", chatId, linkDto.uri()));
        links.get(linkDto.linkType()).remove(linkDto.uri());
    }

    public Map<LinkType, List<URI>> getLinks(Long chatId) {
        log.debug(String.format("get links for the chat{id=%s}", chatId));
        return links.entrySet().stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
