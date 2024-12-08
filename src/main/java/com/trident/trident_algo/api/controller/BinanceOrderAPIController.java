package com.trident.trident_algo.api.controller;

import com.trident.trident_algo.api.model.BinanceOrderRequest;
import com.trident.trident_algo.api.model.OrderResponse;
import com.trident.trident_algo.api.service.OrderAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/rest")
public class BinanceOrderAPIController {

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
    public Mono<ResponseEntity<String>> placeOrder(@RequestBody BinanceOrderRequest binanceOrderRequest) throws Exception {
        return orderAPIService.placeOrder(binanceOrderRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

    @GetMapping("/openOrders")
    public Mono<ResponseEntity<List<OrderResponse>>> getOpenOrders(@RequestParam String symbol) throws Exception {
        return orderAPIService.getOpenOrders(symbol)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Collections.emptyList())));

    }

    @DeleteMapping("/order")
    public Mono<ResponseEntity<String>> deleteOrder(@RequestParam String symbol, @RequestParam String orderId) throws Exception {
        return orderAPIService.deleteOrder(symbol, orderId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));

    }

    @DeleteMapping("/allOpenOrders")
    public Mono<ResponseEntity<String>> deleteAllOpenOrders(@RequestParam String symbol) throws Exception {
        return orderAPIService.deleteAllOpenOrders(symbol)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));

    }


}
