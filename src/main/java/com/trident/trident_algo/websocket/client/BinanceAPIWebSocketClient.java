package com.trident.trident_algo.websocket.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class BinanceAPIWebSocketClient extends AbstractBinanceWebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceAPIWebSocketClient.class);
    private final ObjectMapper mapper = new ObjectMapper();


    private final ReactorNettyWebSocketClient client;

    @Autowired
    @Qualifier("binanceAPISession")
    private AtomicReference<WebSocketSession> sessionRef ;

    private final String wssURL;

    public BinanceAPIWebSocketClient(String url,
                                     @Qualifier("binanceAPIReactorWebSocket") ReactorNettyWebSocketClient client,
                                     @Qualifier("binanceAPISession") AtomicReference<WebSocketSession> sessionRef){

        this.wssURL = url;
        this.client = client;
        this.sessionRef = sessionRef;
    }


    @Override
    public Mono<Void> connect(List<Map<String, Object>> requestList, int step, int spreadPercent) {
        return client.execute(URI.create(wssURL), session -> {
            sessionRef.set(session);
            LOGGER.info("Connected to Binance WebSocket! {}", wssURL);

            try {
                if (session.isOpen()) {
                    Flux<Void> loop = Flux.fromStream(IntStream.rangeClosed(1, step).boxed())
                            .flatMap(i -> {
                                try {
                                    String binanceAPImessage = mapper.writeValueAsString(requestList.get(i-1));
                                    Mono<Void> send = session.send(Mono.just(session.textMessage(binanceAPImessage)))
                                            .doOnSuccess(aVoid -> LOGGER.info("Sent: {} (iteration {})", binanceAPImessage, i))
                                            .doOnError(e -> LOGGER.error("Failed to send message: {}", e.getMessage()));

                                    Flux<WebSocketMessage> receive = session.receive()
                                            .doOnNext(message -> LOGGER.info("Received: {} (iteration {})", message.getPayloadAsText(), i))
                                            .doOnError(e -> LOGGER.error("Failed to receive message: {}", e.getMessage()));

                                    return send.thenMany(receive).then();
                                } catch (JsonProcessingException e) {
                                    return Mono.error(e);
                                }
                            });

                    return loop.then()
                            .doFinally(signalType -> {
                                LOGGER.info("WebSocket session completed with signal: {}", signalType);
                                sessionRef.set(null);  // Clear the session reference after completion
                            });
                } else {
                    LOGGER.warn("Cannot send message, no open session.");
                    return Mono.empty();
                }
            } catch (Exception e) {
                LOGGER.error("Error processing message: {}", e.getMessage());
                return Mono.error(e);
            }
        }).then(disconnect());
    }

    @Override
    public Mono<Void> connect() {
        //No implementation needed
        return null;
    }

    public Mono<Void> disconnect() {
        return super.disconnect(sessionRef);
    }

}
