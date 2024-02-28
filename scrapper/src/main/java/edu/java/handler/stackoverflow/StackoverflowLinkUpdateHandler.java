package edu.java.handler.stackoverflow;

import edu.java.enums.LinkType;
import edu.java.handler.LinkUpdateHandler;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StackoverflowLinkUpdateHandler extends LinkUpdateHandler {

    public StackoverflowLinkUpdateHandler(List<StackoverflowSource> sources) {
        super(LinkType.STACKOVERFLOW, sources);
    }
}
