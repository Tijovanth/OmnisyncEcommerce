package com.example.inventory_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreateRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        Long stockQuantity,

        @NotNull(message = "Price is required")
        Double price,

        @NotNull(message = "Category is required")
        Long categoryId
) {
}
