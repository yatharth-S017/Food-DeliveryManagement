package com.foodordering.foodservice.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "foods")
public class Food implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private Double price;

    private String category;

    @Column(nullable = false)
    private Boolean availability = true;

    @Column(nullable = false)
    private Long restaurantId;

    public Food() {}

    public Food(String foodName, Double price, String category, Boolean availability, Long restaurantId) {
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
