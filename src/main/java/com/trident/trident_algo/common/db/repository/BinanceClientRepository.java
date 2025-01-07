package com.trident.trident_algo.common.db.repository;

import com.trident.trident_algo.common.db.entity.BinanceClient;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BinanceClientRepository extends ReactiveMongoRepository<BinanceClient, String> {
    Mono<Boolean> existsByKey(String key);

    Mono<Boolean> existsBySecret(String secret);
}
