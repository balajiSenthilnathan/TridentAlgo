package com.trident.trident_algo.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BinanceAPIWebSocketRequestParams {

    private String symbol;
    private String side;
    private String type;
    private String price;
    private String positionSide;
    private String quantity;
    private String timeInForce;
    private String apiKey;
    private String signature;

}
