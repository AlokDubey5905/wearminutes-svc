package com.wearminutes.service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
}
