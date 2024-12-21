package com.trident.trident_algo.api.model;

import lombok.Data;

import java.util.List;

@Data
public class BinanceOrderDeleteRequest {

    private String symbol;
    private List<Long> orderIds;
}
