package com.wearminutes.service.dto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "shops")
@Getter
@Setter
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "shop_name", nullable = false, length = 200)
    private String shopName;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;
}
