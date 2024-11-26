package com.trident.trident_algo;

import com.trident.trident_algo.websocket.client.AbstractBinanceWebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TridentAlgoApplication implements CommandLineRunner {

	private final AbstractBinanceWebSocketClient binanceFutureWebSocketClient;
	private final AbstractBinanceWebSocketClient binanceAPIWebSocketClient;

	@Autowired
	public TridentAlgoApplication(
			@Qualifier("binanceFutureWebSocket") AbstractBinanceWebSocketClient binanceFutureWebSocketClient,
			@Qualifier("binanceAPIWebSocket") AbstractBinanceWebSocketClient binanceAPIWebSocketClient) {
		this.binanceFutureWebSocketClient = binanceFutureWebSocketClient;
		this.binanceAPIWebSocketClient = binanceAPIWebSocketClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(TridentAlgoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		binanceFutureWebSocketClient.connect();
		binanceAPIWebSocketClient.connect();
	}
}
