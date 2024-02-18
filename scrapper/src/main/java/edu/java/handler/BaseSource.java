package edu.java.handler;

import edu.java.entity.Link;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public interface BaseSource<T extends BaseSource<T>> extends ChainElement<T> {

    String urlPrefix();

    String urlPattern();

    void checkLinkUpdate(Link link);

    void processLinkChain(Link link);

    default MatchResult linkMatcher(Link link) {
        return Pattern.compile(urlPattern())
            .matcher(link.getUrl())
            .results()
            .toList()
            .getFirst();
    }
}
