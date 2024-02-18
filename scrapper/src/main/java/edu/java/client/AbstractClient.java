package edu.java.client;

import edu.java.exception.LinkSourceError;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract class AbstractClient {

    private final WebClient webClient;

    protected AbstractClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> getLinkUpdates(
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
            .onStatus(
                status -> status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST),
                res -> res.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(
                        LinkSourceError.BROKEN_LINK.toException(res.request().getURI() + " - " + body))
                    )
            )
            .bodyToMono(responseType);
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
