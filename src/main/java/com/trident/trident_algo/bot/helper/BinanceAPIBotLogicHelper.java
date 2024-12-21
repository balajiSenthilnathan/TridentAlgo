package com.trident.trident_algo.bot.helper;

import com.trident.trident_algo.common.model.BinanceSymbolPrecision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class BinanceAPIBotLogicHelper {

    private final Map<String, BinanceSymbolPrecision> exchangePrecisionMap;

    public BinanceAPIBotLogicHelper(@Qualifier("binanceExchangePrecisionMap") Map<String, BinanceSymbolPrecision> precisionMap) {
        this.exchangePrecisionMap = precisionMap;
    }


    public Map<String, String> calculatePriceBasedOnSpread(String side, String price, double spread, int stepValue) {
        Map<String, String> priceComposite = new HashMap<>();
        priceComposite.put("ERROR", null);

        if (Objects.isNull(side) || side.isEmpty())
            priceComposite.put("ERROR", "Side is Null/Empty");

        if (Objects.isNull(price) || price.isEmpty())
            priceComposite.put("ERROR", "price is Null/Empty");

        // Price calculation logic
        BigDecimal difference = BigDecimal.valueOf(Double.parseDouble(price) * ((spread * stepValue) / 100.0d)).setScale(4, RoundingMode.DOWN);
        if ("BUY".equalsIgnoreCase(side))
            priceComposite.put("revisedPrice", String.valueOf(BigDecimal.valueOf(Double.parseDouble(price)).subtract(difference)));
        else if ("SELL".equalsIgnoreCase(side))
            priceComposite.put("revisedPrice", String.valueOf(BigDecimal.valueOf(Double.parseDouble(price)).add(difference)));

        return priceComposite;
    }

    public String calculateQtyBasedOnQtyByUSDT(String price, String qtyByUSDT, String symbol) {
        return BigDecimal.valueOf(Double.parseDouble(qtyByUSDT)/Double.parseDouble(price))
                .setScale(exchangePrecisionMap.get(symbol).getStepAssetPrecision(),
                        RoundingMode.DOWN).toString();
    }

}
