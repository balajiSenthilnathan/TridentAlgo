package com.trident.trident_algo.common.db.dao;

import com.trident.trident_algo.common.db.entity.OrderSettings;
import reactor.core.publisher.Mono;

public interface OrderSettingsDAO {

    Mono<OrderSettings> updateAutoOrderSettings(OrderSettings settings);
    Mono<OrderSettings> fetchSettings();

}
