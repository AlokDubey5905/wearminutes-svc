package com.wearminutes.service.dto.request;

import com.wearminutes.service.dto.entity.DeliveryTask;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryTaskUpdateRequest {
    private DeliveryTask.Status status;
    private String deliveryOtp;
}
