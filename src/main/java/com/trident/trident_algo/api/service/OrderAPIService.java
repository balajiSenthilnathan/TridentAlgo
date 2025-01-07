package com.trident.trident_algo.api.service;

import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.trident.trident_algo.api.helper.BinanceSignatureHelper;
import com.trident.trident_algo.api.helper.CommonServiceHelper;
import com.trident.trident_algo.api.helper.OrderValidationHelper;
import com.trident.trident_algo.api.model.BinanceMarketPriceResponse;
import com.trident.trident_algo.api.model.BinanceOrderDeleteRequest;
import com.trident.trident_algo.api.model.BinanceOrderResponse;
import com.trident.trident_algo.api.model.BinanceOrderRequest;
import com.trident.trident_algo.common.db.dao.OrderSettingsDAO;
import com.trident.trident_algo.common.db.entity.OrderSettings;
import com.trident.trident_algo.common.helper.BinanceAPIBotLogicHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class OrderAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAPIService.class);

    private final BinanceAPIBotLogicHelper binanceAPIBotLogicHelper;

    private final OrderValidationHelper orderValidationHelper;

    private final OrderSettingsDAO orderSettingsDAO;

    private final WebSocketStreamClientImpl websocketClient;


    @Autowired
    public OrderAPIService(BinanceAPIBotLogicHelper binanceAPIBotLogicHelper, com.trident.trident_algo.api.helper.OrderValidationHelper orderValidationHelper, OrderSettingsDAO orderSettingsDAO,
                           @Qualifier("publicWebSocketStream") WebSocketStreamClientImpl websocketClient) {
        this.binanceAPIBotLogicHelper = binanceAPIBotLogicHelper;
        this.orderValidationHelper = orderValidationHelper;
        this.orderSettingsDAO = orderSettingsDAO;
        this.websocketClient = websocketClient;
    }

    @Autowired
    private WebClient webClient;

    public Mono<String> isHedgeModeEnabled() throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new HashMap<>();

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());
        String url = "positionSide/dual?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> LOGGER.info("Position Side response fetched in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()))
                .onErrorResume(RuntimeException.class, e -> Mono.just(e.getMessage()));
    }

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
                .onErrorResume(RuntimeException.class, e -> Mono.just(e.getMessage()));
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

    public Mono<List<BinanceOrderResponse>> placeOrder(List<BinanceOrderRequest> binanceOrderRequests) {
        Instant startInstant = Instant.now();
        return Flux.fromIterable(binanceOrderRequests)
                .flatMap(binanceOrderRequest -> {

                    //Order Validation
                    if (orderValidationHelper.isInvalidOrder(binanceOrderRequest))
                        return Mono.error(new IllegalArgumentException("Side should be greater than 0 for LIMIT orders"));

                    Map<String, Object> params = new TreeMap<>();

                    params.put("symbol", binanceOrderRequest.getSymbol());
                    params.put("side", binanceOrderRequest.getSide());
                    params.put("type", binanceOrderRequest.getType());
                    if (binanceOrderRequest.getModifiers().getIsHedgeMode())
                        params.put("positionSide", binanceOrderRequest.getPositionSide());
                    params.put("quantity", binanceOrderRequest.getQuantity());
                    //params.put("reduceOnly", false);

                    // ORDER for LIMIT type
                    if ("LIMIT".equals(binanceOrderRequest.getType())) {
                        params.put("timeInForce", binanceOrderRequest.getTimeInForce());
                        return Flux.fromStream(IntStream.rangeClosed(1, binanceOrderRequest.getModifiers().getStep()).boxed())
                                .flatMap(stepValue -> {
                                    try {
                                        //String finalSide = "BOTH".equals(binanceOrderRequest.getSide()) ? params.get("side").toString() : binanceOrderRequest.getSide();
                                        params.put("price", getRevisedPrice(
                                                binanceOrderRequest.getSide(),
                                                binanceOrderRequest.getPrice(),
                                                binanceOrderRequest.getModifiers().getSpreadPercent(),
                                                stepValue));
                                        params.put("timeInForce", binanceOrderRequest.getTimeInForce());

                                        //Changes for QTY BY USDT
                                        params.put("quantity", getRevisedQtyByUSDT(params.get("price").toString(),
                                                binanceOrderRequest.getQuantity(), binanceOrderRequest.getModifiers().getQuantityType(),
                                                binanceOrderRequest.getSymbol()));

                                        List<String> limitSignatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, System.currentTimeMillis()); //getServerTime().block());

                                        String url = "order?" + limitSignatureComposite.get(1) + "&signature=" + limitSignatureComposite.get(0);
                                        return makePostCall(url, "Order Placed through REST with price {} in {} ms", params.get("price"), startInstant);
                                    } catch (Exception ex) {
                                        LOGGER.error("Exception occurred while placing LIMIT order: {}", ex.getMessage());
                                        return Mono.empty();
                                    }
                                })
                                .collectList()
                                .flatMapMany(Flux::fromIterable);
                    }
                    // ORDER for MARKET type
                    else {
                        //Changes for QTY BY USDT
                        BinanceMarketPriceResponse marketPriceComposite;
                        try {
                            marketPriceComposite = getMarketPriceBySymbol(binanceOrderRequest.getSymbol()).block();
                            params.put("quantity", getRevisedQtyByUSDT(marketPriceComposite.getPrice(),
                                    binanceOrderRequest.getQuantity(), binanceOrderRequest.getModifiers().getQuantityType(),
                                    binanceOrderRequest.getSymbol()));

                            List<String> signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, System.currentTimeMillis());//getServerTime().block());

                            String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);
                            return makePostCall(url, "Order Placed through REST in {} ms", marketPriceComposite.getPrice(), startInstant)
                                    .flatMap(result -> Mono.just(Collections.singletonList(result)))
                                    .flatMapMany(Flux::fromIterable);
                        } catch (Exception e) {
                            LOGGER.error("Exception occurred while placing MARKET order: {}", e.getMessage());
                            return Mono.empty();
                        }

                    }
                }).collectList();
    }

    public Mono<List<BinanceOrderResponse>> getOpenOrders(String symbol) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", symbol);
        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, System.currentTimeMillis());//getServerTime().block());

        String url = "openOrders?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(BinanceOrderResponse.class)
                .collectList()
                .doOnNext(orderResponses -> LOGGER.info("Fetched orders: {}", orderResponses))
                .onErrorResume(WebClientResponseException.class, e -> Mono.error(new RuntimeException(e.getResponseBodyAsString())))
                .doOnTerminate(() -> LOGGER.info("Fetched all open orders for SYMBOL {} in {} ms", symbol,
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> deleteOrder(String symbol, String orderId) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", symbol);
        params.put("orderId", orderId);

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, System.currentTimeMillis());//getServerTime().block());
        String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, e -> Mono.just(e.getResponseBodyAsString()))
                .doOnTerminate(() -> LOGGER.info("Open order {} deleted in {} ms", orderId,
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> deleteOrderReactive(String symbol, String orderId) {
        Instant startInstant = Instant.now();
        Map<String, Object> params = new TreeMap<>();

        params.put("symbol", symbol);
        params.put("orderId", orderId);

        return BinanceSignatureHelper.getHmacSha256SignatureReactive(params, System.currentTimeMillis())
                .flatMap(signatureComposite -> {
                    String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

                    return webClient.delete()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .onErrorResume(WebClientResponseException.class, e -> Mono.just(e.getResponseBodyAsString()))
                            .doOnTerminate(() -> LOGGER.info("Open order {} cancelled in {} ms", orderId,
                                    Duration.between(startInstant, Instant.now()).toMillis()));
                });
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
                .doOnTerminate(() -> LOGGER.info("Deleted all open orders for SYMBOL {} in {} ms", symbol,
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<String> closeTimeoutOrders() throws Exception {
        long currentTime = Instant.now().toEpochMilli();
        long fiveMinutesAgo = currentTime - (CommonServiceHelper.getTimeOut() * 60 * 1000);
        String symbol = CommonServiceHelper.getSymbol();
        return getOpenOrders(symbol)
                .flatMapMany(Flux::fromIterable)
                .filter(order -> order.getUpdateTime() < fiveMinutesAgo) //validPriceChanges(order)
                .flatMap(order -> {
                    try {
                        return deleteOrder(symbol, String.valueOf(order.getOrderId()));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to delete order", e));
                    }
                })
                .collectList()
                .map(results -> "Closed " + results.size() + " orders.")
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }

    private Mono<Boolean> validPriceChanges(BinanceOrderResponse order) throws Exception {
        final boolean[] status = {false};
        return getMarketPriceBySymbol(order.getSymbol())
                .map(marketPriceObject -> {
                    double orderPrice = Double.parseDouble(order.getPrice());
                    double priceChangePercentage;
                    if ("SELL".equals(order.getSide())) {
                        priceChangePercentage = ((Double.parseDouble(marketPriceObject.getPrice()) - orderPrice) / orderPrice) * 100;
                        return priceChangePercentage >= CommonServiceHelper.getCutoff();
                    } else if ("BUY".equals(order.getSide())) {
                        priceChangePercentage = ((orderPrice - Double.parseDouble(marketPriceObject.getPrice())) / orderPrice) * 100;
                        return priceChangePercentage >= CommonServiceHelper.getCutoff();
                    }
                    return false;
                });
    }

    public Mono<List<String>> deleteOrderByIds(BinanceOrderDeleteRequest binanceOrderDeleteRequest) {
        return Flux.fromIterable(binanceOrderDeleteRequest.getOrderIds())
                .flatMap(orderId -> {
                    try {
                        return deleteOrder(binanceOrderDeleteRequest.getSymbol(), String.valueOf(orderId));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .collectList();
    }

    // Get revised price based on spread
    private String getRevisedPrice(String side, String price, double spread, int stepValue) {
        Map<String, String> priceComposite = binanceAPIBotLogicHelper.calculatePriceBasedOnSpread(side, price, spread, stepValue);
        return Objects.isNull(priceComposite.get("ERROR")) ?
                priceComposite.get("revisedPrice") : price;
    }

    private String getRevisedQtyByUSDT(String price, String qty, String quantityType, String symbol) {
        if ("USDT".equals(quantityType))
            return binanceAPIBotLogicHelper.calculateQtyBasedOnQtyByUSDT(price, qty, symbol);
        else
            return qty;
    }

    // Fetch Binance Server Time
    private Mono<Long> getServerTime() {
        return webClient.get()
                .uri("time")
                .retrieve()
                .bodyToMono(Map.class)  // Map the response body to a Map
                .map(response -> (Long) response.get("serverTime"));
    }

    private Mono<BinanceOrderResponse> makePostCall(String url, String resultLog, Object... logParams) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(BinanceOrderResponse.class)
                .doOnTerminate(() -> LOGGER.info(resultLog, logParams[0].toString(), Duration.between((Instant) logParams[1], Instant.now()).toMillis()))
                .onErrorResume(RuntimeException.class, e -> {
                    LOGGER.error("Error in making post call: {}", e.getMessage());
                    return Mono.error(e); // Return an empty or default instance
                });
    }

    public Mono<BinanceMarketPriceResponse> getMarketPriceBySymbol(String symbol) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", symbol);

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());

        String url = "ticker/price?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BinanceMarketPriceResponse.class)
                .doOnTerminate(() -> LOGGER.info("Market price retrieved in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()))
                .onErrorResume(RuntimeException.class, e -> {
                    LOGGER.error("Error retrieving market price: {}", e.getMessage());
                    return Mono.just(new BinanceMarketPriceResponse()); // Return an empty or default instance
                });
    }

    //TEST
    public Mono<String> getAllOpenPositions() throws Exception {
        WebClient customWebCLient = WebClient.builder().baseUrl("https://fapi.binance.com")
                .defaultHeader("X-MBX-APIKEY", "ImiKIIv5cHZZLVEKxPHWshsWWkGVOWaJlfpcAUVU6CmgpHrvCctcfZX7eKI5nPy6").build();
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new HashMap<>();

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());
        String url = "/fapi/v3/positionRisk?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return customWebCLient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> LOGGER.info("Response from Binance API: {}", response))
                .doOnTerminate(() -> LOGGER.info("Open positions response fetched in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()))
                .onErrorResume(RuntimeException.class, e -> Mono.just(e.getMessage()));
    }

    public Mono<String> getApiTradingStatus() throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, Object> params = new HashMap<>();

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params, getServerTime().block());
        String url = "apiTradingStatus?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> LOGGER.info("API Tracker status from Binance API: {}", response))
                .doOnTerminate(() -> LOGGER.info("API Tracker status fetched in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()))
                .onErrorResume(RuntimeException.class, e -> Mono.just(e.getMessage()));
    }

    /* ORDER SETTINGS */
    /* Method to retrieve Bot order settings*/
    public Mono<OrderSettings> fetchOrderSettings() {
        return orderSettingsDAO.fetchSettings();
    }

    /* Method to save Bot order settings*/
    public Mono<OrderSettings> saveOrUpdateOrderSettings(OrderSettings orderSettings) {
        return orderSettingsDAO.updateAutoOrderSettings(orderSettings);
    }

    /* AUTO PLACE/CLOSE ORDER */
    public Mono<List<BinanceOrderResponse>> placeAndMonitorOrdersWithTargetPrice(
            List<BinanceOrderRequest> binanceOrderRequests) {
        LOGGER.info("INSIDE");
        return placeOrder(binanceOrderRequests) // Call the existing method
                .flatMapMany(Flux::fromIterable)
                .doOnNext(orderResponse -> {
                    // Trigger monitoring asynchronously for each placed order
                    fetchTargetPriceAndMonitor(orderResponse)
                            .subscribe(
                                    null,
                                    error -> LOGGER.error("Error monitoring order {}: {}", orderResponse.getOrderId(), error.getMessage()),
                                    () -> LOGGER.info("Monitoring completed for orderId: {}", orderResponse.getOrderId())
                            );
                })
                .collectList(); // Return the placed orders immediately
    }

    private Mono<BinanceOrderResponse> fetchTargetPriceAndMonitor(BinanceOrderResponse orderResponse) {
        String symbol = orderResponse.getSymbol();

        return fetchOrderSettings()
                .map(settings ->
                        binanceAPIBotLogicHelper.calculateTargetPrice(orderResponse.getPrice(), settings.getDeviationValue()))
                .flatMap(targetPrice -> monitorPrice(symbol, targetPrice, orderResponse.getOrderId().toString())
                        .thenReturn(orderResponse)); // Return original response after monitoring
    }

    private Mono<Void> monitorPrice(String symbol, BigDecimal targetPrice, String orderId) {
        return Flux.<BigDecimal>create(sink -> {
                    // Subscribe to the aggregate trade stream for the given symbol
                    int streamId = websocketClient.aggTradeStream(symbol.toLowerCase(), event -> {
                        try {
                            // Parse the JSON string to extract the price
                            JsonObject jsonObject = JsonParser.parseString(event).getAsJsonObject();
                            BigDecimal currentPrice = new BigDecimal(jsonObject.get("p").getAsString()); // "p" = price

                            // Log the current market price
                            System.out.println("Current Market Price for " + symbol + ": " + currentPrice);

                            // Emit the price to the sink
                            sink.next(currentPrice);

                            // Complete the sink if the target price is reached
                            if (currentPrice.compareTo(targetPrice) >= 0) {
                                sink.complete();
                            }
                        } catch (Exception e) {
                            sink.error(e); // Handle parsing or other exceptions
                        }
                    });

                    // Handle disposal of the WebSocket connection when the Flux is canceled or completed
                    sink.onDispose(() -> {
                        System.out.println("Disposing WebSocket connection for streamId: " + streamId);
                        websocketClient.closeConnection(streamId);
                    });
                })
                .filter(price -> price.compareTo(targetPrice) >= 0) // Filter for prices meeting the condition
                .take(1) // Stop after the first matching price
                .flatMap(price -> deleteOrderReactive(symbol, orderId)) // Delete the order reactively
                .doOnError(e -> System.err.println("Error in WebSocket stream: " + e.getMessage())) // Log WebSocket errors
                .doFinally(signalType -> {
                    System.out.println("WebSocket connection finalized for symbol: " + symbol);
                })
                .then();
    }
}
