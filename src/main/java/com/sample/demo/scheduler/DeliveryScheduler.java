package com.sample.demo.scheduler;

import com.sample.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryScheduler {

    private final OrderService orderService;

    /**
     * Daily cronjob that checks scheduled deliveries and marks them as FULFILLED
     * Runs every day at 00:01 AM
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void fulfillScheduledDeliveries() {
        log.info("========== Running daily delivery fulfillment check ==========");
        try {
            orderService.fulfillScheduledDeliveries();
        } catch (Exception e) {
            log.error("Error occurred during delivery fulfillment check", e);
        }
        log.info("========== Delivery fulfillment check completed ==========");
    }
}
