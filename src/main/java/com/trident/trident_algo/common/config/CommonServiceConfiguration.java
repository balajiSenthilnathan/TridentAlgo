package com.trident.trident_algo.common.config;

import com.trident.trident_algo.common.model.BinanceSymbolPrecision;
import com.trident.trident_algo.common.model.ExchangeInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class CommonServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonServiceConfiguration.class);

    private final Map<String, BinanceSymbolPrecision> precisionMap = new ConcurrentHashMap<>();
    private final WebClient webClient;

    public CommonServiceConfiguration(WebClient webClient) {
        this.webClient = webClient;
    }

    @Bean
    @Qualifier("binanceExchangePrecisionMap")
    public Map<String, BinanceSymbolPrecision> getPrecisionMap() {
        return precisionMap;
    }

    @PostConstruct
    public void init() {
        updateBinanceExchangePrecisionMap();
    }

    @Scheduled(cron = "${binance.precision.update.cron}")
    public void updateBinanceExchangePrecisionMap() {
        Instant startInstant = Instant.now();

        webClient.get()
                .uri("exchangeInfo")
                .retrieve()
                .bodyToMono(ExchangeInfo.class)
                .doOnTerminate(() -> LOGGER.info("Exchange Info response fetched in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()))
                .doOnError(error -> LOGGER.error("Error response from Binance API", error))
                .onErrorResume(WebClientResponseException.class, e -> {
                    LOGGER.error("Error response from Binance API: {}", e.getResponseBodyAsString(), e);
                    return Mono.error(new RuntimeException("Failed to fetch exchange info from Binance API", e));
                })
                .map(exchangeInfo -> exchangeInfo.getSymbols().stream()
                        .collect(Collectors.toMap(
                                ExchangeInfo.SymbolInfo::getSymbol,
                                symbolInfo -> new BinanceSymbolPrecision(symbolInfo.getQuantityPrecision(),
                                        symbolInfo.getPricePrecision()))))
                .doOnSuccess(updatedMap -> {
                    precisionMap.clear();
                    precisionMap.putAll(updatedMap);
                    LOGGER.info("Precision map updated successfully.");
                })
                .doOnError(error -> LOGGER.error("Error updating precision map", error))
                .subscribe();
    }
}
