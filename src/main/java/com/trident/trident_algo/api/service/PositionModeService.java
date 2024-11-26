package com.trident.trident_algo.api.service;

import com.trident.trident_algo.api.helper.BinanceSignatureHelper;
import com.trident.trident_algo.api.model.PositionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PositionModeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionModeService.class);

    @Autowired
    private WebClient webClient;

    public Mono<String> enableHedgeMode(Boolean enableFlag) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, String> params = new HashMap<>();
        params.put("dualSidePosition", String.valueOf(enableFlag));
        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params);

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

    public Mono<String> openPositionByMode(PositionParams positionParams) throws Exception {
        Instant startInstant = Instant.now();
        List<String> signatureComposite;
        Map<String, String> params = new HashMap<>();
        params.put("symbol", positionParams.getSymbol());
        params.put("side", positionParams.getSide());
        params.put("positionSide", positionParams.getPositionSide());
        params.put("type", positionParams.getType());
        params.put("quantity", positionParams.getQuantity());

        signatureComposite = BinanceSignatureHelper.getHmacSha256Signature(params);

        String url = "order?" + signatureComposite.get(1) + "&signature=" + signatureComposite.get(0);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(String.class)
                .doOnTerminate(() -> LOGGER.info("Order API executed in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

}
