package edu.java.bot.configuration;

import edu.java.bot.client.ScrapperClient;
import edu.java.bot.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@RequiredArgsConstructor
@Component
public class ClientConfig {

    private final ApplicationConfig applicationConfig;

    @Bean
    public ScrapperClient scrapperClient() {
        return buildHttpInterface(applicationConfig.scrapperClient().api(), ScrapperClient.class,
            applicationConfig.scrapperClient().retry());
    }

    private <T> T buildHttpInterface(String baseUrl, Class<T> serviceType, ApplicationConfig.RetryConfig retryConfig) {
        WebClientAdapter webClientAdapter = WebClientAdapter.create(
            WebClient.builder()
                .baseUrl(baseUrl)
                .filter(RetryUtil.retryFilter(retryConfig))
                .build());
        return HttpServiceProxyFactory.builderFor(webClientAdapter).build().createClient(serviceType);
    }
}
