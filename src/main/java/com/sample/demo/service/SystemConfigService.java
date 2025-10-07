package com.sample.demo.service;

import com.sample.demo.exception.BadRequestException;
import com.sample.demo.model.entity.SystemConfig;
import com.sample.demo.repository.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;
    public static final String DELIVERY_PERIOD_KEY = "delivery.period.days";

    @PostConstruct
    @Transactional
    public void initialize() {
        if (repository.findByConfigKey(DELIVERY_PERIOD_KEY).isEmpty()) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(DELIVERY_PERIOD_KEY);
            config.setConfigValue("7");
            config.setUpdatedAt(LocalDateTime.now());
            repository.save(config);
            log.info("Initialized delivery period: 7 days");
        }
    }

    @Transactional(readOnly = true)
    public int getDeliveryPeriod() {
        return repository.findByConfigKey(DELIVERY_PERIOD_KEY)
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .orElse(7);
    }

    @Transactional
    public void updateDeliveryPeriod(int days) {
        if (days < 1 || days > 30) {
            throw new BadRequestException("Delivery period must be between 1 and 30 days");
        }

        SystemConfig config = repository.findByConfigKey(DELIVERY_PERIOD_KEY)
                .orElseThrow(() -> new BadRequestException("Configuration not found"));

        config.setConfigValue(String.valueOf(days));
        config.setUpdatedAt(LocalDateTime.now());
        repository.save(config);
        log.info("Updated delivery period to {} days", days);
    }
}
