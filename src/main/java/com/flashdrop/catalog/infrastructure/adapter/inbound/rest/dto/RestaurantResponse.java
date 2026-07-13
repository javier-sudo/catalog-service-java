package com.flashdrop.catalog.infrastructure.adapter.inbound.rest.dto;

import java.time.LocalDateTime;

import com.flashdrop.catalog.domain.model.Restaurant;

public record RestaurantResponse(
        Long id,
        Long userId,
        String name,
        String address,
        LocalDateTime createdAt
) {
    public static RestaurantResponse fromDomain(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getUserId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCreatedAt()
        );
    }
}
