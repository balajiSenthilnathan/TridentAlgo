package com.trident.trident_algo.api.service;

import com.trident.trident_algo.api.helper.BinanceSignatureHelper;
import com.trident.trident_algo.api.model.BinanceOrderRequest;
import com.trident.trident_algo.bot.helper.BinanceAPIBotLogicHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class OrderAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAPIService.class);

    private final BinanceAPIBotLogicHelper binanceAPIBotLogicHelper;

    @Autowired
    public OrderAPIService(BinanceAPIBotLogicHelper binanceAPIBotLogicHelper) {
        this.binanceAPIBotLogicHelper = binanceAPIBotLogicHelper;
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

    public Mono<String> placeOrderThroughREST(BinanceOrderRequest binanceOrderRequest) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", binanceOrderRequest.getSymbol());
        params.put("side", binanceOrderRequest.getSide());
        params.put("type", binanceOrderRequest.getType());
        params.put("positionSide", binanceOrderRequest.getPositionSide());
        params.put("quantity", binanceOrderRequest.getQuantity());
        if("LIMIT".equals(binanceOrderRequest.getType())) {
            params.put("price", getRevisedPrice(binanceOrderRequest.getSide(),
                    binanceOrderRequest.getPrice(), binanceOrderRequest.getSpreadPercent()));
            params.put("timeInForce", binanceOrderRequest.getTimeInForce());
        }

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

        String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> LOGGER.info("Order Placed through REST in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    // Get revised price based on spread
    private String getRevisedPrice(String side, String price, int spread){
        Map<String, String> priceComposite = binanceAPIBotLogicHelper.calculatePriceBasedOnSpread(side, price, spread);
        return Objects.nonNull(priceComposite.get("ERROR")) ?
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

}
