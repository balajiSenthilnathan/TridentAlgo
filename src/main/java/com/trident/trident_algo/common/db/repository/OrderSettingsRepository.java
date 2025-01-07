package com.trident.trident_algo.common.db.repository;

import com.trident.trident_algo.common.db.entity.OrderSettings;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface OrderSettingsRepository extends ReactiveMongoRepository<OrderSettings, String> {

    Mono<OrderSettings> findFirstBy();
}
