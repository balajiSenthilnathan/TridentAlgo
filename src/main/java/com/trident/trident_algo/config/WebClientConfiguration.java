package com.trident.trident_algo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfiguration {

    @Value("${binance.api.key}")
    private String apiKey;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://fapi.binance.com/fapi/v1/")
                .defaultHeader("X-MBX-APIKEY", apiKey)
                .filter(handleErrors())
                //.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();
    }

    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode() == HttpStatus.BAD_REQUEST) {
                // Extract additional information from the response body if needed
                return clientResponse.bodyToMono(String.class)
                        .flatMap(responseBody -> {
                            // You can parse the response body if necessary
                            System.out.println(responseBody);
                            return Mono.error(new RuntimeException("400 Bad Request: " + responseBody));
                        });
            }
            return Mono.just(clientResponse); // Continue processing the response if no error
        });
    }

}
