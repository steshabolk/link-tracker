package edu.java.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GithubClient extends AbstractClient {

    @Autowired
    public GithubClient(@Qualifier("githubWebClient") WebClient webClient) {
        super(webClient);
    }
}
