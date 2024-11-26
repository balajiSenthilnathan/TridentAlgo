package com.trident.trident_algo.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionParams {

    private  String symbol;
    private  String side;
    private  String positionSide;
    private  String type;
    private  String quantity;

}
