package com.foodordering.notificationservice.kafka;

import com.foodordering.notificationservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationConsumer.class);

    @KafkaListener(topics = "order-created", groupId = "notification-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("========================================================================");
        log.info(">>> [KAFKA CONSUMER] Event received from topic 'order-created'!");
        log.info(">>> Payload: OrderId={}, CustomerId={}, TotalAmount=${}, Status={}",
                event.getOrderId(), event.getCustomerId(), event.getTotalAmount(), event.getStatus());
        log.info(">>> [SIMULATED NOTIFICATION SENT TO CUSTOMER #{}]:", event.getCustomerId());
        log.info(">>> \"Your order #{} has been successfully placed.\"", event.getOrderId());
        log.info("========================================================================");
    }
}
