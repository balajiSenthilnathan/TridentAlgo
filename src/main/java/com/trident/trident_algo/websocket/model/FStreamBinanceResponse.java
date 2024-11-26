package com.trident.trident_algo.websocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class FStreamBinanceResponse {

    @JsonProperty("e")
    @JsonPropertyDescription("Event type. In this case, it indicates a trade event")
    private String eventType;

    @JsonProperty("E")
    @JsonPropertyDescription("Event time. This is the timestamp when the trade event was recorded")
    private Long epochTime;

    @JsonProperty("T")
    @JsonPropertyDescription("Trade time. This is the timestamp when the trade actually happened. It can sometimes be the same as the event time")
    private Long tradeTime;

    @JsonProperty("s")
    @JsonPropertyDescription("Symbol. This represents the trading pair involved in the trade")
    private String symbol;

    @JsonProperty("t")
    @JsonPropertyDescription("Trade ID. This is a unique identifier for the trade")
    private Long tradeId;

    @JsonProperty("p")
    @JsonPropertyDescription("Price. This is the price at which the trade was executed")
    private String price;

    @JsonProperty("q")
    @JsonPropertyDescription("Quantity. This is the amount of the base asset (BTC in this case) that was traded")
    private String quantity;

    @JsonProperty("X")
    @JsonPropertyDescription("Order type. This indicates the type of order that triggered the trade")
    private String orderType;

    @JsonProperty("m")
    @JsonPropertyDescription("Is the buyer the market maker/taker")
    private Boolean isMarketMaker;

}