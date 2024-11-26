package com.trident.trident_algo.websocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trident.trident_algo.websocket.model.FStreamBinanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class AbstractBinanceWebSocketClient implements WebSocketClient{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBinanceWebSocketClient.class);

    private final String binanceWSSUrl;

    private final ObjectMapper  mapper = new ObjectMapper();

    public AbstractBinanceWebSocketClient(String url){
        this.binanceWSSUrl = url;
    }

    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();

        client.execute(new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                LOGGER.info("Connected to Binance WebSocket! {}", binanceWSSUrl);
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                LOGGER.debug("Received: {}", message.getPayload());
                FStreamBinanceResponse response = mapper.readValue(message.getPayload().toString(), FStreamBinanceResponse.class);
                LOGGER.info("{}",response);
            }
            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) {
                LOGGER.error("Error: {}", exception.getMessage());
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
                LOGGER.info("Connection closed: {}", closeStatus);
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        }, binanceWSSUrl);
    }

    @Override
    public CompletableFuture<WebSocketSession> execute(WebSocketHandler webSocketHandler, String uriTemplate, Object... uriVariables) {
        return null;
    }

    @Override
    public CompletableFuture<WebSocketSession> execute(WebSocketHandler webSocketHandler, WebSocketHttpHeaders headers, URI uri) {
        return null;
    }
}
