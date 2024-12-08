package com.trident.trident_algo.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderResponse {

    @JsonProperty("orderId")
    private Long orderId;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("price")
    private String price;

    @JsonProperty("origQty")
    private String origQty;

    @JsonProperty("executedQty")
    private String executedQty;

    @JsonProperty("status")
    private String status;

    @JsonProperty("time")
    private Long time;

    @JsonProperty("updateTime")
    private Long updateTime;
}

