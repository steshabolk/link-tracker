package edu.java.handler.stackoverflow;

import edu.java.enums.LinkType;
import edu.java.handler.LinkSource;

public interface StackoverflowSource extends LinkSource {

    @Override
    default String urlDomain() {
        return LinkType.STACKOVERFLOW.getDomain();
    }
}
