package com.wearminutes.service.dto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_shop_id", columnList = "shop_id")
})
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    private Shop shop;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "fabric")
    private String fabric; // e.g., 'Banarasi Silk'

    @Column(name = "color")
    private String color;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version; // Optimistic locking field

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;
}
