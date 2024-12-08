package com.trident.trident_algo.websocket.controller;

import com.trident.trident_algo.websocket.model.BinanceAPIWebSocketRequest;
import com.trident.trident_algo.websocket.service.BinanceWebSocketAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "binance.websocket.enable", havingValue = "true")
@Validated
@RequestMapping("/api/v1/websocket")
public class BinanceWebSocketOrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceWebSocketOrderController.class);

    @Autowired
    private BinanceWebSocketAPIService binanceWebSocketAPIService;

    @PostMapping("/placeOrder")
    public Mono<ResponseEntity<String>> sendWebSocketMessage(@RequestBody BinanceAPIWebSocketRequest binanceAPIWebSocketRequest) throws Exception {
        return binanceWebSocketAPIService.sendWebSocketMessage(binanceAPIWebSocketRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

}
