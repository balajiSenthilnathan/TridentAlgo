package com.trident.trident_algo.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestModifiers {

    private int spreadPercent;
    private int maxOrdersPerSecond;
    private int step;
    private String channel;

}
