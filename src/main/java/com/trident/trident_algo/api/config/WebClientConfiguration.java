package com.trident.trident_algo.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientConfiguration.class);

    @Value("${binance.api.key}")
    private String apiKey;

    // Set larger buffer size (e.g., 10MB)
    ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024)) // Set the buffer size limit to 10 MB
            .build();

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .baseUrl("https://fapi.binance.com/fapi/v1/")
                .defaultHeader("X-MBX-APIKEY", apiKey)
                .filter(handleErrors())
                .build();
    }

    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode() == HttpStatus.BAD_REQUEST) {
                // Extract additional information from the response body if needed
                return clientResponse.bodyToMono(String.class)
                        .flatMap(responseBody -> {
                            // You can parse the response body if necessary
                            LOGGER.error("ERR : {}", responseBody);
                            return Mono.error(new RuntimeException("400 Bad Request: " + responseBody));
                        });
            }
            return Mono.just(clientResponse); // Continue processing the response if no error
        });
    }

}
