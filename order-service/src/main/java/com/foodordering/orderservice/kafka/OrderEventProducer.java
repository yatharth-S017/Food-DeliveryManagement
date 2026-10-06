package com.foodordering.orderservice.kafka;

import com.foodordering.orderservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    public static final String TOPIC = "order-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("=================================================");
            log.info(">>> [KAFKA PRODUCER] Publishing event to topic '{}'", TOPIC);
            log.info(">>> Payload: orderId={}, customerId={}, totalAmount={}, status={}",
                    event.getOrderId(), event.getCustomerId(), event.getTotalAmount(), event.getStatus());
            log.info("=================================================");
            kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event);
        } catch (Exception e) {
            log.warn(">>> [KAFKA PRODUCER] Could not publish event to Kafka (broker might be offline): {}", e.getMessage());
        }
    }
}
