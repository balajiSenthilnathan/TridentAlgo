package com.trident.trident_algo.api.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestModifiers {

    private double spreadPercent;
    private int maxOrdersPerSecond;
    private int step;
    private String channel;
    private String quantityType;
    private Boolean isHedgeMode;

}
