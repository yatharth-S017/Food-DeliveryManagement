package com.foodordering.foodservice.service;

import com.foodordering.foodservice.model.Food;
import com.foodordering.foodservice.repository.FoodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FoodService {

    private static final Logger log = LoggerFactory.getLogger(FoodService.class);
    private final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    @Cacheable(value = "foods", key = "#id")
    public Optional<Food> getFoodById(Long id) {
        log.info("=================================================");
        log.info(">>> [REDIS CACHE MISS] Food ID: {} NOT found in Redis!", id);
        log.info(">>> Querying MySQL database directly...");
        log.info(">>> Storing fetched result into Redis cache...");
        log.info("=================================================");
        return foodRepository.findById(id);
    }

    public Food createFood(Food food) {
        return foodRepository.save(food);
    }

    @CacheEvict(value = "foods", key = "#id")
    public Optional<Food> updateFood(Long id, Food updatedFood) {
        log.info("=================================================");
        log.info(">>> [REDIS CACHE EVICTION] Updating Food ID: {}", id);
        log.info(">>> Evicting stale entry from Redis cache...");
        log.info("=================================================");
        return foodRepository.findById(id).map(existing -> {
            existing.setFoodName(updatedFood.getFoodName());
            existing.setPrice(updatedFood.getPrice());
            existing.setCategory(updatedFood.getCategory());
            existing.setAvailability(updatedFood.getAvailability());
            existing.setRestaurantId(updatedFood.getRestaurantId());
            return foodRepository.save(existing);
        });
    }

    @CacheEvict(value = "foods", key = "#id")
    public boolean deleteFood(Long id) {
        if (!foodRepository.existsById(id)) {
            return false;
        }
        log.info("=================================================");
        log.info(">>> [REDIS CACHE EVICTION] Deleting Food ID: {}", id);
        log.info(">>> Evicting entry from Redis cache...");
        log.info("=================================================");
        foodRepository.deleteById(id);
        return true;
    }
}
