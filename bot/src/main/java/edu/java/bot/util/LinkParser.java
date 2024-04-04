package edu.java.bot.util;

import edu.java.bot.enums.BotReply;
import java.net.URI;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkParser {

    private static final String URL_PATTERN = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";

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
        boolean isSupportedSource = LinkSourceUtil.isSupportedSource(link.getHost(), url);
        if (!isSupportedSource) {
            throw new RuntimeException(BotReply.NOT_SUPPORTED_SOURCE.getReply());
        }
        return link;
    }
}
