package com.flashdrop.catalog.infrastructure.adapter.inbound.rest.dto;

import java.math.BigDecimal;

// DTO de entrada: representa el JSON que llega al hacer POST /catalog/products.
public record CreateProductRequest(
        Long categoryId,
        Long restaurantId,
        String name,
        String description,
        BigDecimal price,
        String image,
        Boolean available
) {
}
