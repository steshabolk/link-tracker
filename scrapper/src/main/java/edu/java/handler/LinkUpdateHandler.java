package edu.java.handler;

import edu.java.entity.Link;
import edu.java.enums.LinkType;

public interface LinkUpdateHandler {

    LinkType linkType();

    void updateLink(Link link);
}
