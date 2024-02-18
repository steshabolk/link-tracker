package edu.java.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class StackoverflowClient extends AbstractClient {

    @Autowired
    public StackoverflowClient(@Qualifier("stackoverflowWebClient") WebClient webClient) {
        super(webClient);
    }
}
