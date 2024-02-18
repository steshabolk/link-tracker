package edu.java.handler;

import edu.java.entity.Link;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSource<T extends BaseSource<T>> implements BaseSource<T> {

    private T next;

    @Override
    public void setNext(T source) {
        next = source;
    }

    @Override
    public T getNext() {
        return next;
    }

    @Override
    public void processLinkChain(Link link) {
        if (Pattern.matches(urlPattern(), link.getUrl())) {
            checkLinkUpdate(link);
            return;
        }
        Optional.ofNullable(getNext())
            .ifPresentOrElse(
                next -> next.processLinkChain(link),
                () -> log.warn("link cannot be processed: {}", link.getUrl())
            );
    }
}
