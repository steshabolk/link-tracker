package edu.java.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    private final ApplicationConfig applicationConfig;

    @Autowired
    public ClientConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Bean("githubWebClient")
    public WebClient githubClient() {
        return WebClient.builder().baseUrl(applicationConfig.client().githubApi()).build();
    }

    @Bean("stackoverflowWebClient")
    public WebClient stackoverflowClient() {
        return WebClient.builder().baseUrl(applicationConfig.client().stackoverflowApi()).build();
    }
}
