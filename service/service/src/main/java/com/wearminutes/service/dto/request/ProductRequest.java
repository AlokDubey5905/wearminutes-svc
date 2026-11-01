package com.wearminutes.service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    private Long shopId;
    private String title;
    private String fabric;
    private String color;
    private Double price;
    private Integer stockQuantity;
    private String imageUrl;
}
