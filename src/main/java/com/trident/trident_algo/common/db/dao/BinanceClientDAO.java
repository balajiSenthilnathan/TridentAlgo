package com.trident.trident_algo.common.db.dao;

import com.trident.trident_algo.common.db.entity.BinanceClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BinanceClientDAO {
    Flux<BinanceClient> fetchAllClients();
    Mono<BinanceClient> saveClient(BinanceClient client);
    Mono<BinanceClient> fetchClientById(String id);
    Mono<Void> deleteClientById(String id);
}
