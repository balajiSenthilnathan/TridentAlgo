package com.trident.trident_algo.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeInfo {
    private List<SymbolInfo> symbols;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymbolInfo{
        private String symbol;
        private int pricePrecision;
        private int quantityPrecision;

    }

}
