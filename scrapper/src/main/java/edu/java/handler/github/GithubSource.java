package edu.java.handler.github;

import edu.java.enums.LinkType;
import edu.java.handler.BaseSource;

public interface GithubSource extends BaseSource<GithubSource> {

    @Override
    default String urlPrefix() {
        return "https://" + LinkType.GITHUB.getDomain();
    }
}
