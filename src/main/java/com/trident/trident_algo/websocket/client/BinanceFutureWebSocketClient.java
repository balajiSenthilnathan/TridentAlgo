package com.trident.trident_algo.websocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trident.trident_algo.websocket.model.FStreamBinanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BinanceFutureWebSocketClient extends AbstractBinanceWebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceFutureWebSocketClient.class);
    private final ObjectMapper mapper = new ObjectMapper();


    private final ReactorNettyWebSocketClient client;
    private final AtomicReference<WebSocketSession> sessionRef ;
    private final String wssURL;

    public BinanceFutureWebSocketClient(String url,
                                     @Qualifier("binanceFutureReactorWebSocket") ReactorNettyWebSocketClient client,
                                     @Qualifier("binanceFutureSession") AtomicReference<WebSocketSession> sessionRef){
        this.wssURL = url;
        this.client = client;
        this.sessionRef = sessionRef;
    }

    @Override
    public Mono<Void> connect(Map<String, Object> request) {
        return client.execute(URI.create(wssURL), session -> {
            sessionRef.set(session);
            LOGGER.info("Connected to Binance WebSocket! {}", wssURL);

            return session.receive()
                    .doOnNext(this::handleMessage)
                    .doOnError(this::handleError)
                    .doFinally(signalType -> {
                        sessionRef.set(null);
                        LOGGER.info("Connection closed: {}", signalType);
                    }).then();
        });
    }

    private Mono<Void> handleMessage(WebSocketMessage message) {
        try {
            LOGGER.debug("Received: {}", message.getPayloadAsText());
            FStreamBinanceResponse response = mapper.readValue(message.getPayloadAsText(), FStreamBinanceResponse.class);
            LOGGER.info("{}",response);
        } catch (Exception e) {
            LOGGER.error("Error processing message: {}", e.getMessage());
        }
        return null;
    }

    private void handleError(Throwable throwable) {
        LOGGER.error("WebSocket error: {}", throwable.getMessage());
    }

    public Mono<Void> disconnect() {
        return super.disconnect(sessionRef);
    }
}
