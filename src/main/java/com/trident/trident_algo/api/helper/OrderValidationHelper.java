package com.trident.trident_algo.api.helper;

import com.trident.trident_algo.api.model.BinanceOrderRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderValidationHelper {

    public boolean isInvalidOrder(BinanceOrderRequest binanceOrderRequest){
        return "LIMIT".equals(binanceOrderRequest.getType()) && binanceOrderRequest.getModifiers().getStep() <= 0;
    }

}
