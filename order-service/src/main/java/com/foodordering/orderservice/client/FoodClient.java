package com.foodordering.orderservice.client;

import com.foodordering.orderservice.dto.FoodResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "food-service", url = "${services.food-service.url:http://localhost:8082}")
public interface FoodClient {

    @GetMapping("/foods/{id}")
    FoodResponse getFood(@PathVariable("id") Long id);
}
