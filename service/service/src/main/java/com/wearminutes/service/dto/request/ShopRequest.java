package com.wearminutes.service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopRequest {

    private Long vendorId;
    private String shopName;
    private String ownerName;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
}
