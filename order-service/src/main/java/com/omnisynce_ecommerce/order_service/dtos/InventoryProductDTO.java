package com.omnisynce_ecommerce.order_service.dtos;

public record InventoryProductDTO(
        Long id,
        Double price,
        Long stockQuantity
) {
}