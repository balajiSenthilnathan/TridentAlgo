package com.trident.trident_algo.api.service;

import com.trident.trident_algo.common.db.dao.BinanceClientDAO;
import com.trident.trident_algo.common.db.entity.BinanceClient;
import com.trident.trident_algo.common.helper.SecureEncryptionHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
@Validated
@RequiredArgsConstructor
public class ClientManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientManagementService.class);

    @Autowired
    private BinanceClientDAO clientDAO;
    private final SecureEncryptionHelper encryptionService;

    public Flux<BinanceClient> fetchAllClients() {
        Instant startInstant = Instant.now();

        return clientDAO.fetchAllClients()
                .doOnNext(binanceClient ->  LOGGER.info("DB data {}", binanceClient))
                .doOnTerminate(() -> LOGGER.info("Position Side response fetched in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<BinanceClient> persistClient(@Valid BinanceClient client) {
        Instant startInstant = Instant.now();
        // Encrypt sensitive fields
        //client.setKey(encryptionService.encrypt(client.getKey()));
        //client.setSecret(encryptionService.encrypt(client.getSecret()));

        return clientDAO.saveClient(client)
                .doOnNext(binanceClient ->  LOGGER.info("Client {} saved in DB", binanceClient.getName()))
                .doOnTerminate(() -> LOGGER.info("Client persisted in DB in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }

    public Mono<BinanceClient> fetchClientById(String id) {
        Instant startInstant = Instant.now();
        LOGGER.info("Retrieving BinanceClient by ID: {}", id);

        return clientDAO.fetchClientById(id)
                .doOnNext(binanceClient ->  LOGGER.info("Client {} fetched from DB", binanceClient.getName()))
                .doOnTerminate(() -> LOGGER.info("Client fetched in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));

    }

    public Mono<Void> deleteClientById(String id) {
        Instant startInstant = Instant.now();
        LOGGER.info("Attempting to delete BinanceClient with ID: {}", id);
        return clientDAO.deleteClientById(id)
                .doOnNext(binanceClient ->  LOGGER.info("Client id {} deleted from DB", id))
                .doOnTerminate(() -> LOGGER.info("Client deleted in {} ms",
                        Duration.between(startInstant, Instant.now()).toMillis()));
    }


}
