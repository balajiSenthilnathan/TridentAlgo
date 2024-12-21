package com.trident.trident_algo.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinanceMarketPriceResponse {

    private String symbol;
    private String price;
    private long time;

}
