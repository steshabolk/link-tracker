package edu.java.handler.stackoverflow;

import edu.java.enums.LinkType;
import edu.java.handler.AbstractLinkUpdateHandler;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StackoverflowLinkUpdateHandler extends AbstractLinkUpdateHandler<StackoverflowSource> {

    public StackoverflowLinkUpdateHandler(List<StackoverflowSource> sources) {
        super(LinkType.STACKOVERFLOW, sources);
    }
}
