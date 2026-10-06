package com.foodordering.orderservice.dto;

public class FoodResponse {
    private Long foodId;
    private String foodName;
    private Double price;
    private String category;
    private Boolean availability;
    private Long restaurantId;

    public FoodResponse() {}

    public FoodResponse(Long foodId, String foodName, Double price, String category, Boolean availability, Long restaurantId) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.price = price;
        this.category = category;
        this.availability = availability;
        this.restaurantId = restaurantId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}
