package com.omnisynce_ecommerce.order_service.events;

import java.util.List;

public record OrderCreatedEvent(

        Long orderId,
        String orderNumber,
        Double totalAmount,
        List<OrderItemDto> orderItemDto
) implements OutBoxable{

    @Override
    public String getEventType() {
        return "OrderCreatedEvent";
    }

    @Override
    public String getTopic() {
        return "orders_topic";
    }

    @Override
    public String getAggregateType() {
        return "Order";
    }

    @Override
    public Long getAggregateId() {
        return this.orderId;
    }

    @Override
    public Long getCorrelationId() {
        return this.orderId;
    }

    public record OrderItemDto(
            Long productId,
            Long quantity,
            Double price
    ) {}

}
