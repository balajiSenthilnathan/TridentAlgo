package com.trident.trident_algo.api.helper;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BinanceSignatureHelper {

    private static String secretKey;

    @Value("${binance.secret.key}")
    public void setSecretKey(String key) {
        secretKey = key;
    }

    public static List<String> getHmacSha256Signature(Map<String, String> queryParams) throws Exception {
        String queryString = getQueryString(queryParams);
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
        return Arrays.asList(Hex.encodeHexString(hash), queryString);
    }

    private static String  getQueryString(Map<String, String> queryParams) throws Exception {
        queryParams.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return queryParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

    }
}
