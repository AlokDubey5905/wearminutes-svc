package com.wearminutes.service.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeliveryTaskEvent {

    private Long orderId;

    public DeliveryTaskEvent() {
    }

    @JsonCreator
    public DeliveryTaskEvent(@JsonProperty("orderId") Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
