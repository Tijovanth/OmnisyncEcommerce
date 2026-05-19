package com.example.inventory_service.events;

import java.util.List;

public record OrderCreatedEvent(Long orderId,
                                String orderNumber,
                                Double totalAmount,
                                List<OrderItemDto> orderItemDto) {

    public record OrderItemDto(
            Long productId,
            Long quantity,
            Double price
    ) {}

}
