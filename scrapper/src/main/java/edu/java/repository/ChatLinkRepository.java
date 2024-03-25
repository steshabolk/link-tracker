package edu.java.repository;

import edu.java.entity.Chat;
import edu.java.entity.Link;

public interface ChatLinkRepository {

    boolean isLinkAddedToChat(Chat chat, Link link);

    boolean addLinkToChat(Chat chat, Link link);

    boolean removeLinkFromChat(Chat chat, Link link);
}
