package com.trident.trident_algo.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BinanceOrderRequest {

    private  String symbol;
    private  String side;
    private  String positionSide;
    private  String type;
    private  String quantity;
    private  String price;
    private  String timeInForce;
    private int spreadPercent;
    private int maxOrdersPerSecond;
    private int step;

}
