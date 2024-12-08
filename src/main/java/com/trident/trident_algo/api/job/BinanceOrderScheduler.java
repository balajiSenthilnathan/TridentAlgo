package com.trident.trident_algo.api.job;

import com.trident.trident_algo.api.service.OrderAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "binance.scheduler.enable", havingValue = "true")
@Component
//@DependsOn("com.trident.trident_algo.api.helper.CommonServiceHelper")
public class BinanceOrderScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceOrderScheduler.class);
    private final OrderAPIService binanceService;

    @Autowired
    public BinanceOrderScheduler(OrderAPIService binanceService) {
        this.binanceService = binanceService;
    }

    @Scheduled(fixedRateString="${binance.scheduler.fixedRate:300000}")
    public void scheduleCloseOldOrders() throws Exception {
        binanceService.closeTimeoutOrders()
                .subscribe(result -> LOGGER.debug("Cancel order job called : {}", result),
                        error -> LOGGER.error("Error occurred during cancel order job : {}", error.getMessage())
                );
    }
}