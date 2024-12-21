package com.trident.trident_algo.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BinanceOrderResponse {

    /**
     * Order ID.
     */
    @JsonProperty("orderId")
    private Long orderId;

    /**
     * Symbol of the order, e.g., "BTCUSDT".
     */
    @JsonProperty("symbol")
    private String symbol;

    /**
     * Status of the order, e.g., "NEW", "FILLED".
     */
    @JsonProperty("status")
    private String status;

    /**
     * Client order ID.
     */
    @JsonProperty("clientOrderId")
    private String clientOrderId;

    /**
     * Price at which the order was placed.
     */
    @JsonProperty("price")
    private String price;

    /**
     * Average price of the order.
     */
    @JsonProperty("avgPrice")
    private String avgPrice;

    /**
     * Original quantity of the order.
     */
    @JsonProperty("origQty")
    private String origQty;

    /**
     * Executed quantity of the order.
     */
    @JsonProperty("executedQty")
    private String executedQty;

    /**
     * Cumulative quote quantity.
     */
    @JsonProperty("cumQuote")
    private String cumQuote;

    /**
     * Time in force, e.g., "GTC" (Good Till Cancelled).
     */
    @JsonProperty("timeInForce")
    private String timeInForce;

    /**
     * Type of the order, e.g., "LIMIT", "MARKET".
     */
    @JsonProperty("type")
    private String type;

    /**
     * Indicates if the order is reduce only.
     */
    @JsonProperty("reduceOnly")
    private boolean reduceOnly;

    /**
     * Indicates if the order is to close a position.
     */
    @JsonProperty("closePosition")
    private boolean closePosition;

    /**
     * Side of the order, e.g., "BUY", "SELL".
     */
    @JsonProperty("side")
    private String side;

    /**
     * Position side, e.g., "LONG", "SHORT".
     */
    @JsonProperty("positionSide")
    private String positionSide;

    /**
     * Stop price.
     */
    @JsonProperty("stopPrice")
    private String stopPrice;

    /**
     * Working type, e.g., "CONTRACT_PRICE".
     */
    @JsonProperty("workingType")
    private String workingType;

    /**
     * Indicates if price protection is enabled.
     */
    @JsonProperty("priceProtect")
    private boolean priceProtect;

    /**
     * Original type of the order.
     */
    @JsonProperty("origType")
    private String origType;

    /**
     * Price match.
     */
    @JsonProperty("priceMatch")
    private String priceMatch;

    /**
     * Self trade prevention mode.
     */
    @JsonProperty("selfTradePreventionMode")
    private String selfTradePreventionMode;

    /**
     * Good till date.
     */
    @JsonProperty("goodTillDate")
    private Long goodTillDate;

    /**
     * Time when the order was created.
     */
    @JsonProperty("time")
    private Long time;

    /**
     * Time when the order was last updated.
     */
    @JsonProperty("updateTime")
    private Long updateTime;
}


