package com.trident.trident_algo.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BinanceSymbolPrecision {
    private int stepAssetPrecision;
    private int priceAssetPrecision;


    public BinanceSymbolPrecision(int stepAssetPrecision, int priceAssetPrecision) {
        this.stepAssetPrecision = stepAssetPrecision;
        this.priceAssetPrecision = priceAssetPrecision;
    }

}


