package com.foodordering.orderservice.service;

import com.foodordering.orderservice.client.FoodClient;
import com.foodordering.orderservice.client.PaymentClient;
import com.foodordering.orderservice.dto.*;
import com.foodordering.orderservice.kafka.OrderEventProducer;
import com.foodordering.orderservice.model.Order;
import com.foodordering.orderservice.model.OrderItem;
import com.foodordering.orderservice.repository.OrderItemRepository;
import com.foodordering.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FoodClient foodClient;
    private final PaymentClient paymentClient;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        FoodClient foodClient,
                        PaymentClient paymentClient,
                        OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.foodClient = foodClient;
        this.paymentClient = paymentClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info(">>> [STEP 1] Validating food items via OpenFeign FoodClient...");
        double calculatedTotal = 0.0;
        List<OrderItem> itemsToSave = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            FoodResponse food;
            try {
                food = foodClient.getFood(itemReq.getFoodId());
            } catch (Exception e) {
                throw new RuntimeException("Food Service communication error: " + e.getMessage());
            }

            if (food == null || Boolean.FALSE.equals(food.getAvailability())) {
                throw new IllegalStateException("Food item #" + itemReq.getFoodId() + " is currently unavailable.");
            }

            double itemTotal = food.getPrice() * itemReq.getQuantity();
            calculatedTotal += itemTotal;

            itemsToSave.add(new OrderItem(null, food.getFoodId(), itemReq.getQuantity(), food.getPrice()));
        }

        // Save base order
        Order order = new Order(
                request.getCustomerId(),
                request.getRestaurantId(),
                LocalDateTime.now(),
                calculatedTotal,
                "PENDING"
        );
        Order savedOrder = orderRepository.save(order);

        // Save order items
        for (OrderItem item : itemsToSave) {
            item.setOrderId(savedOrder.getOrderId());
            orderItemRepository.save(item);
        }

        log.info(">>> [STEP 2] Processing payment of ${} via OpenFeign PaymentClient...", calculatedTotal);
        PaymentResponse paymentResponse;
        try {
            paymentResponse = paymentClient.processPayment(
                    new PaymentRequest(savedOrder.getOrderId(), calculatedTotal)
            );
        } catch (Exception e) {
            log.warn("Payment client failed or simulated offline: {}", e.getMessage());
            paymentResponse = new PaymentResponse(null, savedOrder.getOrderId(), calculatedTotal, "FAILED", LocalDateTime.now());
        }

        if (paymentResponse != null && "SUCCESS".equalsIgnoreCase(paymentResponse.getPaymentStatus())) {
            savedOrder.setStatus("PLACED");
            orderRepository.save(savedOrder);

            log.info(">>> [STEP 3] Order successfully placed! Publishing event to Kafka topic 'order-created'...");
            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getOrderId(),
                    savedOrder.getCustomerId(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getStatus()
            );
            orderEventProducer.publishOrderCreated(event);
        } else {
            savedOrder.setStatus("PAYMENT_FAILED");
            orderRepository.save(savedOrder);
        }

        return toOrderResponse(savedOrder, itemsToSave,
                paymentResponse != null ? paymentResponse.getPaymentStatus() : "FAILED");
    }

    public Optional<OrderResponse> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                    return toOrderResponse(order, items, null);
                });
    }

    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        List<OrderResponse> responses = new ArrayList<>();
        for (Order o : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(o.getOrderId());
            responses.add(toOrderResponse(o, items, null));
        }
        return responses;
    }

    public Optional<OrderResponse> cancelOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus("CANCELLED");
                    Order updated = orderRepository.save(order);
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
                    return toOrderResponse(updated, items, null);
                });
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> items, String paymentStatus) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                items,
                paymentStatus
        );
    }
}
