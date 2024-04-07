package edu.java.handler;

import edu.java.entity.Link;
import edu.java.util.LinkSourceUtil;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public interface LinkUpdateHandler {

    String regex();

    Optional<String> getLinkUpdate(Link link);

    default MatchResult linkMatcher(Link link) {
        return Pattern.compile("https://" + LinkSourceUtil.getDomain(link.getLinkType()) + regex())
            .matcher(link.getUrl())
            .results()
            .toList()
            .getFirst();
    }
}
