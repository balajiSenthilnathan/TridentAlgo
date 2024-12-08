package com.trident.trident_algo.api.service;

import com.trident.trident_algo.api.helper.BinanceSignatureHelper;
import com.trident.trident_algo.api.helper.CommonServiceHelper;
import com.trident.trident_algo.api.helper.OrderValidationHelper;
import com.trident.trident_algo.api.model.BinanceOrderRequest;
import com.trident.trident_algo.api.model.OrderResponse;
import com.trident.trident_algo.bot.helper.BinanceAPIBotLogicHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class OrderAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAPIService.class);

    private final BinanceAPIBotLogicHelper binanceAPIBotLogicHelper;

    private final OrderValidationHelper orderValidationHelper;

    @Autowired
    public OrderAPIService(BinanceAPIBotLogicHelper binanceAPIBotLogicHelper, com.trident.trident_algo.api.helper.OrderValidationHelper orderValidationHelper) {
        this.binanceAPIBotLogicHelper = binanceAPIBotLogicHelper;
        this.orderValidationHelper = orderValidationHelper;
    }
    @Autowired
    private WebClient webClient;

    public Mono<String> enableHedgeMode(Boolean enableFlag) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new HashMap<>();
        params.put("dualSidePosition", String.valueOf(enableFlag));

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

        String url = "positionSide/dual?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> LOGGER.info("Hedge Mode modified in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()))
                .onErrorResume(RuntimeException.class, e -> Mono.just("Fallback value for error: " + e.getMessage()));
    }

    public Mono<String> openPositionByMode(BinanceOrderRequest binanceOrderRequest) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();
        params.put("symbol", binanceOrderRequest.getSymbol());
        params.put("side", binanceOrderRequest.getSide());
        params.put("positionSide", binanceOrderRequest.getPositionSide());
        params.put("type", binanceOrderRequest.getType());
        params.put("quantity", binanceOrderRequest.getQuantity());

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

        String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> LOGGER.info("Order API executed in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> placeOrder(BinanceOrderRequest binanceOrderRequest) throws Exception {
        Instant startInstant = Instant.now();
        //Order Validation
        if(orderValidationHelper.isInvalidOrder(binanceOrderRequest))
            return Mono.just("Side should be greater than 0 for LIMIT orders");

        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", binanceOrderRequest.getSymbol());
        params.put("side", binanceOrderRequest.getSide());
        params.put("type", binanceOrderRequest.getType());
        params.put("positionSide", binanceOrderRequest.getPositionSide());
        params.put("quantity", binanceOrderRequest.getQuantity());

        // ORDER for LIMIT type
        if ("LIMIT".equals(binanceOrderRequest.getType())) {
            params.put("timeInForce", binanceOrderRequest.getTimeInForce());
            return Flux.fromStream(IntStream.rangeClosed(1, binanceOrderRequest.getModifiers().getStep()).boxed())
                    .flatMap(stepValue -> {
                        try {
                            params.put("price", getRevisedPrice(
                                    binanceOrderRequest.getSide(),
                                    binanceOrderRequest.getPrice(),
                                    binanceOrderRequest.getModifiers().getSpreadPercent(),
                                    stepValue));
                            params.put("timeInForce", binanceOrderRequest.getTimeInForce());
                            List<String> limitSignatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

                            String url = "order?" + limitSignatureComposite.get(1) + "&signature=" + limitSignatureComposite.get(0);
                            return makePostCall(url,"Order Placed through REST with price {} in {} ms", params.get("price"), startInstant);
                        } catch (Exception ex) {
                            LOGGER.error("Exception {}", ex.getMessage());
                            return Mono.empty();
                        }
                    })
                    .collectList()
                    .map(responses -> String.join(",", responses));
        }
        // ORDER for MARKET type
        else{
            signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

            String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);
            return makePostCall(url, "Order Placed through REST in {} ms", startInstant);
        }
    }

    public Mono<List<OrderResponse>> getOpenOrders(String symbol) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", symbol);
        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, System.currentTimeMillis());//getServerTime().block());

        String url = "openOrders?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(OrderResponse.class)
                .collectList()
                .onErrorResume(WebClientResponseException.class, e -> Mono.error(new RuntimeException(e.getResponseBodyAsString())))
                .doOnTerminate(() -> LOGGER.info("Fetched all open orders for SYMBOL {} in {} ms",symbol,
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> deleteOrder(String symbol, String orderId) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", symbol);
        params.put("orderId", orderId);

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());
        String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(e.getResponseBodyAsString()))
                .doOnTerminate(() -> LOGGER.info("Open order {} deleted in {} ms", orderId,
                Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> deleteAllOpenOrders(String symbol) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", symbol);
        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

        String url = "allOpenOrders?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(e.getResponseBodyAsString()))
                .doOnTerminate(() -> LOGGER.info("Deleted all open orders for SYMBOL {} in {} ms",symbol,
                Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> closeTimeoutOrders() throws Exception {
        long currentTime = Instant.now().toEpochMilli();
        long fiveMinutesAgo = currentTime - (CommonServiceHelper.getTimeOut() * 60 * 1000);
        String symbol = CommonServiceHelper.getSymbol();
        return getOpenOrders(symbol)
                .flatMapMany(Flux::fromIterable)
                .filter(order -> order.getUpdateTime() < fiveMinutesAgo)
                .flatMap(order -> {
                    try {
                        return deleteOrder(symbol, String.valueOf(order.getOrderId()));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to delete order", e));
                    }
                })
                .collectList()
                .map(results -> "Closed " + results.size() + " orders.")
                .onErrorResume(e -> Mono.just("Error: " + e.getMessage()));
    }

    // Get revised price based on spread
    private String getRevisedPrice(String side, String price, int spread, int stepValue){
        Map<String, String> priceComposite = binanceAPIBotLogicHelper.calculatePriceBasedOnSpread(side, price, spread, stepValue);
        return Objects.isNull(priceComposite.get("ERROR")) ?
                priceComposite.get("revisedPrice") : price;
    }

    // Fetch Binance Server Time
    private Mono<Long> getServerTime() {
        return webClient.get()
                .uri("time")
                .retrieve()
                .bodyToMono(Map.class)  // Map the response body to a Map
                .map(response -> (Long) response.get("serverTime"));
    }

    private Mono<String> makePostCall(String url, String resultLog, Object...logParams){
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> LOGGER.info(resultLog, logParams[0].toString(), Duration.between((Instant) logParams[1], Instant.now()).toMillis()));
    }

}
