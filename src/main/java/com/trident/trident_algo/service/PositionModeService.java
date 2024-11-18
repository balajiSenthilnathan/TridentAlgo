package com.trident.trident_algo.service;

import com.trident.trident_algo.helper.BinanceSignatureHelper;
import com.trident.trident_algo.model.PositionParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PositionModeService {

    @Autowired
    private WebClient webClient;

    public Mono<String> enableHedgeMode(Boolean enableFlag) throws Exception {
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
                .onErrorResume(RuntimeException.class, e -> {
                    // Return a fallback value when an error occurs
                    return Mono.just("Fallback value for error: " + e.getMessage());
                });
    }

    public Mono<String> openPositionByMode(PositionParams positionParams) throws Exception {

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
                .bodyToMono(String.class);
    }

}
