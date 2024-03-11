package edu.java.util;

import edu.java.entity.Link;
import edu.java.exception.ApiExceptionType;
import java.net.URI;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkParser {

    private static final String URL_PATTERN = "^(http(s)?)://(www\\.)?[a-zA-Z\\d@:%._~=#&?/+-]+$";

    public static Link parseLink(URI link) {
        String url = link.toString();
        if (!Pattern.matches(URL_PATTERN, url)) {
            throw ApiExceptionType.INVALID_LINK.toException();
        }
        return LinkTypeUtil.getLinkType(link.getHost(), url)
            .map(it -> Link.builder()
                .linkType(it)
                .url("https://" + url.substring(url.indexOf(it.getDomain())))
                .build())
            .orElseThrow(ApiExceptionType.NOT_SUPPORTED_SOURCE::toException);
    }
}
