package com.foodordering.orderservice.dto;

import java.util.List;

public class OrderRequest {
    private Long customerId;
    private Long restaurantId;
    private List<OrderItemRequest> items;

    public OrderRequest() {}

    public OrderRequest(Long customerId, Long restaurantId, List<OrderItemRequest> items) {
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
