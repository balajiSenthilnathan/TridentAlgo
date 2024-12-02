package com.trident.trident_algo.websocket.config;

import com.trident.trident_algo.websocket.client.BinanceAPIWebSocketClient;
import com.trident.trident_algo.websocket.client.BinanceFutureWebSocketClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

@ConditionalOnProperty(name = "binance.websocket.enable", havingValue = "true")
@Configuration
public class BinanceWebSocketConfiguration {

    @Value("${binance.websocket.future.coin.live.data}")
    private String binanceFutureWSSUrl;

    @Value("${binance.webSocket.spot.api}")
    private String binanceAPIWSSUrl;

    @Bean
    @Qualifier("binanceFutureReactorWebSocket")
    public ReactorNettyWebSocketClient binanceFutureReactorWebSocket() {
        return new ReactorNettyWebSocketClient();
    }

    @Bean
    @Qualifier("binanceFutureSession")
    public AtomicReference<WebSocketSession> binanceFutureSession() {
        return new AtomicReference<>();
    }

    @Bean
    @Qualifier("binanceFutureWebSocket")
    public BinanceFutureWebSocketClient binanceFutureWebSocket(
            @Qualifier("binanceFutureReactorWebSocket") ReactorNettyWebSocketClient client,
            @Qualifier("binanceFutureSession") AtomicReference<WebSocketSession> sessionRef) throws URISyntaxException {
        return new BinanceFutureWebSocketClient(binanceFutureWSSUrl, client, sessionRef);
    }

    // Binance API Websocket
    @Bean
    @Qualifier("binanceAPIReactorWebSocket")
    public ReactorNettyWebSocketClient binanceAPIReactorWebSocket() {
        return new ReactorNettyWebSocketClient();
    }

    @Bean
    @Qualifier("binanceAPISession")
    public AtomicReference<WebSocketSession> binanceAPISession() {
        return new AtomicReference<>();
    }

    @Bean
    @Qualifier("binanceAPIWebSocket")
    public BinanceAPIWebSocketClient binanceAPIWebSocket(
            @Qualifier("binanceAPIReactorWebSocket") ReactorNettyWebSocketClient client,
            @Qualifier("binanceAPISession") AtomicReference<WebSocketSession> sessionRef) throws URISyntaxException {
        return new BinanceAPIWebSocketClient(binanceAPIWSSUrl, client, sessionRef);
    }

}