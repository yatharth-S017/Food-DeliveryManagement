package com.foodordering.orderservice.dto;

public class OrderItemRequest {
    private Long foodId;
    private Integer quantity;

    public OrderItemRequest() {}

    public OrderItemRequest(Long foodId, Integer quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
