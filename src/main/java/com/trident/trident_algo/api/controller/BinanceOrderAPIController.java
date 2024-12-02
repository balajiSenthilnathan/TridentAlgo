package com.trident.trident_algo.api.controller;

import com.trident.trident_algo.api.model.BinanceOrderRequest;
import com.trident.trident_algo.api.service.OrderAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/order")
public class BinanceOrderAPIController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceOrderAPIController.class);

    @Autowired
    private OrderAPIService orderAPIService;

    @GetMapping("/enableHedgeMode")
    public ResponseEntity<Mono<String>> enableHedgeMode(@RequestParam Boolean enableFlag) throws Exception {
        Mono<String> response = orderAPIService.enableHedgeMode(enableFlag);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/openPosition")
    public Mono<ResponseEntity<String>> openPositionByMode(@RequestBody BinanceOrderRequest binanceOrderRequest) throws Exception {
        return orderAPIService.openPositionByMode(binanceOrderRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @PostMapping("/placeOrder")
    public Mono<ResponseEntity<String>> sendWebSocketMessage(@RequestBody BinanceOrderRequest binanceOrderRequest) throws Exception {
        return orderAPIService.placeOrderThroughREST(binanceOrderRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

}
