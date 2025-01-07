package com.trident.trident_algo.common.db.dao.impl;

import com.trident.trident_algo.common.db.dao.OrderSettingsDAO;
import com.trident.trident_algo.common.db.entity.OrderSettings;
import com.trident.trident_algo.common.db.repository.OrderSettingsRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
@AllArgsConstructor
public class OrderSettingsDAOImpl implements OrderSettingsDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSettingsDAOImpl.class);

    private final OrderSettingsRepository orderSettingsRepository;

    @Override
    public Mono<OrderSettings> updateAutoOrderSettings(OrderSettings settings) {
        settings.setId("ORDER_SETTINGS_STATIC_ID"); // Ensure the ID is fixed for the single record
        settings.setCreatedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());
        return orderSettingsRepository.save(settings);
    }

    @Override
    public Mono<OrderSettings> fetchSettings() {
        return orderSettingsRepository.findFirstBy();
    }
}
