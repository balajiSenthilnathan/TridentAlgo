package com.trident.trident_algo.bot.helper;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class BinanceAPIBotLogicHelper {

    public Map<String, String> calculatePriceBasedOnSpread(String side, String price, int spread) {
        Map<String, String> priceComposite = new HashMap<>();
        priceComposite.put("ERROR", null);

        if (Objects.isNull(side) || side.isEmpty())
            priceComposite.put("ERROR", "Side is Null/Empty");

        if (Objects.isNull(price) || price.isEmpty())
            priceComposite.put("ERROR", "price is Null/Empty");

        // Price calculation logic
        double difference = (Double.parseDouble(price)*(spread/100.0d));
        if ("BUY".equalsIgnoreCase(side))
            priceComposite.put("RevisedPrice", String.valueOf(Double.parseDouble(price) - difference));
        else if ("SELL".equalsIgnoreCase(side))
            priceComposite.put("RevisedPrice", String.valueOf(Double.parseDouble(price) + difference));

        return priceComposite;
    }

}
