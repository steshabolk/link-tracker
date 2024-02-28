package edu.java.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Component
public class ClientConfig {

    private final ApplicationConfig applicationConfig;

    @Bean("githubWebClient")
    public WebClient githubClient() {
        return WebClient.builder().baseUrl(applicationConfig.githubClient().api()).build();
    }

    @Bean("stackoverflowWebClient")
    public WebClient stackoverflowClient() {
        return WebClient.builder().baseUrl(applicationConfig.stackoverflowClient().api()).build();
    }
}
