package edu.java.handler.github;

import edu.java.enums.LinkType;
import edu.java.handler.LinkSource;

public interface GithubSource extends LinkSource {

    @Override
    default String urlDomain() {
        return LinkType.GITHUB.getDomain();
    }
}
