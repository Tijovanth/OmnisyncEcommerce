package com.omnisynce_ecommerce.order_service.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequestDTO(

        Double totalAmount,
        List<OrderItemCreateRequestDTO> orderItems
) {

    public record OrderItemCreateRequestDTO(
            @NotNull(message = "Product id cannot be null")
            Long productId,
            @NotNull(message = "Quantity cannot be null")
            Long quantity,
            Double price
    ){

    }
}
