package com.trident.trident_algo;

import com.trident.trident_algo.websocket.client.BinanceFutureWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TridentAlgoApplication implements CommandLineRunner {

    private final BinanceFutureWebSocketClient binanceFutureWebSocketClient;

    @Value("${binance.websocket.enable}:false")
    private String binanceWebSocketEnabled;

    public TridentAlgoApplication(
            @Autowired(required = false) @Qualifier("binanceFutureWebSocket") BinanceFutureWebSocketClient binanceFutureWebSocketClient) {
        this.binanceFutureWebSocketClient = binanceFutureWebSocketClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(TridentAlgoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if ("true".equals(binanceWebSocketEnabled))
            binanceFutureWebSocketClient.connect(null).subscribe();
    }
}
