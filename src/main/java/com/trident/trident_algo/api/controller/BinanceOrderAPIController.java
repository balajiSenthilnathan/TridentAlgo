package com.trident.trident_algo.api.controller;

import com.trident.trident_algo.api.model.BinanceMarketPriceResponse;
import com.trident.trident_algo.api.model.BinanceOrderDeleteRequest;
import com.trident.trident_algo.api.model.BinanceOrderResponse;
import com.trident.trident_algo.api.model.BinanceOrderRequest;
import com.trident.trident_algo.api.service.OrderAPIService;
import com.trident.trident_algo.common.helper.PayloadWrapperHelper;
import com.trident.trident_algo.common.model.GenericPayloadWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Collections;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/rest")
@CrossOrigin(origins = "*")
public class BinanceOrderAPIController {

    //Server-Sent Events for Timed out Orders
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();


    @Autowired
    private OrderAPIService orderAPIService;

    @GetMapping("/positionMode")
    public Mono<ResponseEntity<String>> getPositionMode() throws Exception {
        return orderAPIService.isHedgeModeEnabled()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));

    }

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
    public Mono<ResponseEntity<GenericPayloadWrapper<List<BinanceOrderResponse>>>> placeOrder(@RequestBody List<BinanceOrderRequest> binanceOrderRequest) throws Exception {
        return orderAPIService.placeOrder(binanceOrderRequest)
                .map(orders -> ResponseEntity.ok(PayloadWrapperHelper.success(orders)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PayloadWrapperHelper.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()))));
    }


    @GetMapping("/openOrders")
    public Mono<ResponseEntity<List<BinanceOrderResponse>>> getOpenOrders(@RequestParam String symbol) throws Exception {
        return orderAPIService.getOpenOrders(symbol)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Collections.emptyList())));

    }

    /*@GetMapping("/openOrders")
    public Mono<ResponseEntity<GenericPayloadWrapper<List<BinanceOrderResponse>>>> getOpenOrders(@RequestParam String symbol) throws Exception {
        return orderAPIService.getOpenOrders(symbol)
                .map(orders -> ResponseEntity.ok(PayloadWrapperHelper.success(orders)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(PayloadWrapperHelper.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()))));
    }*/

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

    @PostMapping("/orderList")
    public Mono<ResponseEntity<List<String>>> deleteOrderByIds(@RequestBody BinanceOrderDeleteRequest binanceOrderDeleteRequest) throws Exception {
        return orderAPIService.deleteOrderByIds(binanceOrderDeleteRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(Collections.emptyList())));

    }

    @GetMapping("/getMarketPrice")
    public ResponseEntity<Mono<BinanceMarketPriceResponse>> getMarketPrice(@RequestParam String symbol) throws Exception {
        Mono<BinanceMarketPriceResponse> response = orderAPIService.getMarketPriceBySymbol(symbol);
        return ResponseEntity.ok(response);
    }
    
    //TEST
    @GetMapping("/getAllOpenPositions")
    public ResponseEntity<Mono<String>> getAllOpenOrders() throws Exception {
        Mono<String> response = orderAPIService.getAllOpenPositions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health-check")
    public Mono<ResponseEntity<String>> testApplication() throws Exception {
        return Mono.just(ResponseEntity.status(200).body("Success"));
    }

    //Server-Sent Events for Timed out Orders
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamOrderEvents() {
        return sink.asFlux();
    }

    public void sendEvent(String event) {
        sink.tryEmitNext(event);
    }
}
