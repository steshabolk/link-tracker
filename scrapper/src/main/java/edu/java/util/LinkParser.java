package edu.java.util;

import edu.java.entity.Link;
import edu.java.enums.LinkType;
import edu.java.exception.ApiExceptionType;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkParser {

    private static final String URL_PATTERN = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";
    private static final List<LinkType> DOMAINS = Arrays.stream(LinkType.values()).toList();

    public static Link parseLink(URI link) {
        String url = link.toString();
        if (!Pattern.matches(URL_PATTERN, url)) {
            throw ApiExceptionType.INVALID_LINK.toException();
        }
        return DOMAINS.stream()
            .filter(it -> it.getDomain().equals(link.getHost()))
            .findFirst()
            .filter(it -> it.isSupportedSource(url.substring(url.indexOf(it.getDomain()))))
            .map(it -> Link.builder()
                .linkType(it)
                .url("https://" + url.substring(url.indexOf(it.getDomain())))
                .build())
            .orElseThrow(ApiExceptionType.NOT_SUPPORTED_SOURCE::toException);
    }
}
