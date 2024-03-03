package edu.java.bot.util;

import edu.java.bot.enums.BotReply;
import edu.java.bot.enums.LinkType;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkParser {

    private static final String URL_PATTERN = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";
    private static final List<LinkType> DOMAINS = Arrays.stream(LinkType.values()).toList();

    public static URI parseLink(String url) {
        if (!Pattern.matches(URL_PATTERN, url)) {
            throw new RuntimeException(BotReply.INVALID_LINK.getReply());
        }
        URI link;
        try {
            link = URI.create(url);
        } catch (RuntimeException ex) {
            throw new RuntimeException(BotReply.INVALID_LINK.getReply());
        }
        DOMAINS.stream()
            .filter(domain -> domain.getDomain().equals(link.getHost()))
            .findFirst()
            .filter(it -> it.isSupportedSource(url.substring(url.indexOf(it.getDomain()))))
            .orElseThrow(() -> new RuntimeException(BotReply.NOT_SUPPORTED_LINK.getReply()));
        return link;
    }
}
