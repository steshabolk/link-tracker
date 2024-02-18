package edu.java.handler.stackoverflow;

import edu.java.enums.LinkType;
import edu.java.handler.BaseSource;

public interface StackoverflowSource extends BaseSource<StackoverflowSource> {

    @Override
    default String urlPrefix() {
        return "https://" + LinkType.STACKOVERFLOW.getDomain();
    }
}
