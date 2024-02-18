package edu.java.bot.util;

import edu.java.bot.dto.LinkDto;
import edu.java.bot.enums.Emoji;
import edu.java.bot.enums.LinkType;
import edu.java.bot.exception.ApiException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkParser {

    private static final String URL_PATTERN = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";
    private static final String INVALID_LINK_REPLY =
        String.format("%s your link is invalid. please try again", Emoji.ERROR.getMarkdown());
    private static final String UNSUPPORTED_LINK_REPLY =
        String.format("%s sorry, tracking is not supported on this resource", Emoji.ERROR.getMarkdown());
    private final List<LinkType> domains = Arrays.stream(LinkType.values()).toList();

    public static LinkDto parseLink(String url) {
        if (!Pattern.matches(URL_PATTERN, url)) {
            throw new ApiException(INVALID_LINK_REPLY);
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (RuntimeException ex) {
            throw new ApiException(INVALID_LINK_REPLY);
        }
        List<LinkType> linkType = domains.stream()
            .filter(domain -> domain.getDomain().equals(uri.getHost()))
            .toList();
        if (linkType.size() != 1) {
            throw new ApiException(UNSUPPORTED_LINK_REPLY);
        }
        return new LinkDto(linkType.get(0), uri);
    }
}
