package edu.java.client;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractClient {

    private static final int TIMEOUT = 2000;

    private final WebClient webClient;

    protected AbstractClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T> Optional<T> get(
        String url,
        Map<String, String> params,
        ParameterizedTypeReference<T> responseType
    ) {
        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(url)
                .queryParams(getQueryParams(params))
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(responseType)
            .blockOptional(Duration.ofMillis(TIMEOUT));
    }

    private MultiValueMap<String, String> getQueryParams(Map<String, String> params) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        if (CollectionUtils.isEmpty(params)) {
            return queryParams;
        }
        params.forEach(queryParams::add);
        return queryParams;
    }
}
