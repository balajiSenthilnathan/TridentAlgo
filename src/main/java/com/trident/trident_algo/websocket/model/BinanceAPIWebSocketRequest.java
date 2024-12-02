package com.trident.trident_algo.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BinanceAPIWebSocketRequest {

    private String id;
    private String method;
    private BinanceAPIWebSocketRequestParams params;
    private int spreadPercent;
    private int maxOrdersPerSecond;

}

