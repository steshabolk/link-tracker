package edu.java.configuration;

import edu.java.client.GithubClient;
import edu.java.client.StackoverflowClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@RequiredArgsConstructor
@Component
public class ClientConfig {

    private static final int TIMEOUT = 2000;

    private final ApplicationConfig applicationConfig;

    @Bean
    public GithubClient githubClient() {
        return buildHttpInterface(applicationConfig.githubClient().api(), GithubClient.class);
    }

    @Bean
    public StackoverflowClient stackoverflowClient() {
        return buildHttpInterface(applicationConfig.stackoverflowClient().api(), StackoverflowClient.class);
    }

    private <T> T buildHttpInterface(String baseUrl, Class<T> serviceType) {
        WebClientAdapter webClientAdapter = WebClientAdapter.create(WebClient.builder().baseUrl(baseUrl).build());
        webClientAdapter.setBlockTimeout(Duration.ofMillis(TIMEOUT));
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build().createClient(serviceType);
    }
}
