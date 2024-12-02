package com.trident.trident_algo.websocket.service;

import com.trident.trident_algo.api.helper.BinanceSignatureHelper;
import com.trident.trident_algo.bot.helper.BinanceAPIBotLogicHelper;
import com.trident.trident_algo.websocket.client.BinanceAPIWebSocketClient;
import com.trident.trident_algo.websocket.model.BinanceAPIWebSocketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@ConditionalOnProperty(name = "binance.websocket.enable", havingValue = "true")
@Service
public class BinanceWebSocketAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceWebSocketAPIService.class);

    private final BinanceAPIWebSocketClient binanceAPIWebSocketClient;
    private final BinanceAPIBotLogicHelper binanceAPIBotLogicHelper;

    public BinanceWebSocketAPIService(
            @Qualifier("binanceAPIWebSocket") BinanceAPIWebSocketClient binanceAPIWebSocketClient, BinanceAPIBotLogicHelper binanceAPIBotLogicHelper) {
        this.binanceAPIWebSocketClient = binanceAPIWebSocketClient;
        this.binanceAPIBotLogicHelper = binanceAPIBotLogicHelper;
    }

    public Mono<String> sendWebSocketMessage(BinanceAPIWebSocketRequest request) throws Exception {
        Instant startInstant = Instant.now();

        List<String> signatureComposite;

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("id", UUID.randomUUID()); // Unique ID
        orderRequest.put("method", request.getMethod());
        Long serverTime = System.currentTimeMillis();//

        Map<String, Object> params = new TreeMap<>();
        params.put("recvWindow", 60000);
        params.put("symbol", request.getParams().getSymbol());
        params.put("side", request.getParams().getSide());
        params.put("type", request.getParams().getType());

        params.put("positionSide", request.getParams().getPositionSide());
        params.put("quantity", request.getParams().getQuantity());

        if("LIMIT".equals(request.getParams().getType())) {
            params.put("price", getRevisedPrice(request.getParams().getSide(),
                    request.getParams().getPrice(), request.getSpreadPercent()));
            params.put("timeInForce", request.getParams().getTimeInForce());
        }

        params.put("apiKey", BinanceSignatureHelper.getApiKey());
        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, serverTime);
        params.put("signature", signatureComposite.get(0));

        orderRequest.put("params", params);

        //WEBSOCKET
        binanceAPIWebSocketClient.connect(orderRequest)
                .then(binanceAPIWebSocketClient.disconnect())
                .subscribe();

        LOGGER.info("WebSocket API Order executed in {} ms",
                Duration.between(startInstant, Instant.now()).toMillis());
        return Mono.just("Order placed through WebSocket successfully");
    }

    // Get revised price based on spread
    private String getRevisedPrice(String side, String price, int spread){
        Map<String, String> priceComposite = binanceAPIBotLogicHelper.calculatePriceBasedOnSpread(side, price, spread);
        return Objects.nonNull(priceComposite.get("ERROR")) ?
                priceComposite.get("revisedPrice") : price;
    }
}
