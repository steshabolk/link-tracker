package edu.java.handler;

import java.util.List;

public interface ChainElement<T> {

    T getNext();

    void setNext(T next);

    static <T extends ChainElement<T>> T buildChain(List<T> chain) {
        if (chain.isEmpty()) {
            return null;
        }
        for (int i = 0; i < chain.size() - 1; i++) {
            chain.get(i).setNext(chain.get(i + 1));
        }
        return chain.getFirst();
    }
}
