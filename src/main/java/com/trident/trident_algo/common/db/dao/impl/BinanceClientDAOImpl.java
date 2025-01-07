package com.trident.trident_algo.common.db.dao.impl;

import com.trident.trident_algo.common.db.dao.BinanceClientDAO;
import com.trident.trident_algo.common.db.entity.BinanceClient;
import com.trident.trident_algo.common.db.repository.BinanceClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public class BinanceClientDAOImpl implements BinanceClientDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceClientDAOImpl.class);

    private final BinanceClientRepository binanceClientRepository;

    public BinanceClientDAOImpl(BinanceClientRepository repository) {
        this.binanceClientRepository = repository;
    }

    @Override
    public Flux<BinanceClient> fetchAllClients() {
        return binanceClientRepository.findAll();
    }

    @Override
    public Mono<BinanceClient> saveClient(BinanceClient client) {
        // Check for unique constraints reactively
        return binanceClientRepository.existsByKey(client.getKey())
                .flatMap(keyExists -> {
                    if (keyExists) {
                        return Mono.error(new IllegalArgumentException("A client with this key already exists"));
                    }
                    return binanceClientRepository.existsBySecret(client.getSecret());
                })
                .flatMap(secretExists -> {
                    if (secretExists) {
                        return Mono.error(new IllegalArgumentException("A client with this secret already exists"));
                    }
                    client.setCreatedAt(LocalDateTime.now());
                    client.setUpdatedAt(LocalDateTime.now());
                    return binanceClientRepository.save(client);
                });
    }

    @Override
    public Mono<BinanceClient> fetchClientById(String id) {
        return binanceClientRepository.findById(id)
                .doOnNext(client -> LOGGER.info("Successfully retrieved BinanceClient: {}", client))
                .doOnError(error -> LOGGER.error("Error retrieving BinanceClient with ID: {}", id, error))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("BinanceClient not found with ID: " + id)));
    }

    @Override
    public Mono<Void> deleteClientById(String id) {
        return binanceClientRepository.deleteById(id)
                .doOnSuccess(aVoid -> LOGGER.info("Successfully deleted BinanceClient with ID: {}", id))
                .doOnError(error -> LOGGER.error("Error deleting BinanceClient with ID: {}", id, error));
    }
}
