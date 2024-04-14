package edu.java.service.sender;

import edu.java.dto.response.LinkUpdate;

public interface UpdateSender {

    boolean send(LinkUpdate update);
}
