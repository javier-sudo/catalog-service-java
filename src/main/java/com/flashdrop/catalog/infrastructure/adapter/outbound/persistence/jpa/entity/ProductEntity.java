package com.flashdrop.catalog.infrastructure.adapter.outbound.persistence.jpa.entity;

import java.math.BigDecimal;

import com.flashdrop.catalog.domain.model.Product;
import com.flashdrop.catalog.domain.valueobjects.Money;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String image;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    protected ProductEntity() {
    }

    public Product toDomain() {
        return new Product(
                id,
                categoryId,
                restaurantId,
                name,
                description,
                new Money(price),
                image,
                available
        );
    }
}
