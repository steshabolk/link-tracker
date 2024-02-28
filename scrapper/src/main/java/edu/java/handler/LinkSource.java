package edu.java.handler;

import edu.java.entity.Link;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public interface LinkSource {

    String urlDomain();

    String urlPath();

    default String urlPattern() {
        return "https://" + urlDomain() + urlPath();
    }

    void checkLinkUpdate(Link link);

    default MatchResult linkMatcher(Link link) {
        return Pattern.compile(urlPattern())
            .matcher(link.getUrl())
            .results()
            .toList()
            .getFirst();
    }
}
