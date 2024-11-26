package com.trident.trident_algo.websocket.config;

import com.trident.trident_algo.websocket.client.AbstractBinanceWebSocketClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Configuration
public class BinanceWebSocketConfiguration {

    @Value("${binance.websocket.future.coin.live.data}")
    private String binanceFutureWSSUrl;

    @Value("${binance.webSocket.api}")
    private String binanceAPIWSSUrl;

    @Bean
    @Qualifier("binanceFutureWebSocket")
    public AbstractBinanceWebSocketClient binanceFutureWebSocket() throws URISyntaxException {
        return new AbstractBinanceWebSocketClient(binanceFutureWSSUrl);
    }

    @Bean
    @Qualifier("binanceAPIWebSocket")
    public AbstractBinanceWebSocketClient binanceAPIWebSocket() throws URISyntaxException {
        return new AbstractBinanceWebSocketClient(binanceAPIWSSUrl);
    }
}