package com.wearminutes.service.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wearminutes.service.dto.entity.Order;

public class OrderPlacedEvent {
    private Order order;

    public OrderPlacedEvent() {
    }

    @JsonCreator
    public OrderPlacedEvent(@JsonProperty("order") Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
