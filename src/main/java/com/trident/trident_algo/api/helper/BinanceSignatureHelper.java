package com.trident.trident_algo.api.helper;

import lombok.Getter;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.xml.crypto.dsig.SignatureMethod.HMAC_SHA256;

@Component
public class BinanceSignatureHelper {

    private static String secretKey;
    @Getter
    private static String apiKey;

    @Value("${binance.secret.key}")
    public void setSecretKey(String key) {
        secretKey = key;
    }

    @Value("${binance.api.key}")
    public void setApiKeyKey(String key) {
        apiKey = key;
    }

    public static List<String> getHmacSha256Signature(Map<String, Object> queryParams, Long serverTimeStamp) throws Exception {
        String queryString = getQueryString(queryParams, serverTimeStamp);
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
        return Arrays.asList(Hex.encodeHexString(hash), queryString);
    }

    private static String  getQueryString(Map<String, Object> queryParams,Long serverTimeStamp) {
        queryParams.put("timestamp", serverTimeStamp); //String.valueOf(System.currentTimeMillis()));
        queryParams.put("recvWindow", 60000);

        return queryParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

    }

    /**
     * Reactive version of getHmacSha256Signature
     */
    public static Mono<List<String>> getHmacSha256SignatureReactive(Map<String, Object> queryParams, Long serverTimeStamp) {
        return getQueryStringReactive(queryParams, serverTimeStamp)
                .flatMap(queryString -> Mono.fromCallable(() -> {
                    Mac mac = Mac.getInstance(HMAC_SHA256);
                    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
                    mac.init(secretKeySpec);
                    byte[] hash = mac.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
                    return Arrays.asList(Hex.encodeHexString(hash), queryString);
                }));
    }

    /**
     * Reactive version of getQueryString
     */
    private static Mono<String> getQueryStringReactive(Map<String, Object> queryParams, Long serverTimeStamp) {
        return Mono.fromCallable(() -> {
            queryParams.put("timestamp", serverTimeStamp); // Add timestamp
            queryParams.put("recvWindow", 60000); // Add recvWindow
            return queryParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        });
    }

}
