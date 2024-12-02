package com.trident.trident_algo.websocket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractBinanceWebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBinanceWebSocketClient.class);

    abstract public Mono<Void> connect(Map<String, Object> request);

    public Mono<Void> disconnect(AtomicReference<WebSocketSession> sessionRef){
        WebSocketSession session = sessionRef.get();
        if (session != null && session.isOpen()) {
            return session.close()
                    .doOnSuccess(aVoid -> LOGGER.info("WebSocket connection closed."))
                    .doOnError(e -> LOGGER.error("Failed to close WebSocket session: {}", e.getMessage()));
        } else {
            LOGGER.warn("No active WebSocket connection to close.");
            return Mono.empty();
        }
    }
}
