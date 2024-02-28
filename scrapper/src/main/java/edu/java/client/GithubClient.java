package edu.java.client;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GithubClient extends AbstractClient {

    public GithubClient(@Qualifier("githubWebClient") WebClient webClient) {
        super(webClient);
    }

    public <T> Optional<T> doGet(String url, Map<String, String> params, ParameterizedTypeReference<T> responseType) {
        return get(url, params, responseType);
    }
}
